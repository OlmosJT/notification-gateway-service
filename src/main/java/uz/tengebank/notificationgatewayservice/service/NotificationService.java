package uz.tengebank.notificationgatewayservice.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uz.tengebank.notificationcontracts.dto.NotificationRequest;
import uz.tengebank.notificationgatewayservice.constants.CommonErrorCode;
import uz.tengebank.notificationgatewayservice.dto.ApiResponse;
import uz.tengebank.notificationgatewayservice.dto.notification.PushPayload;
import uz.tengebank.notificationgatewayservice.dto.notification.SmsPayload;
import uz.tengebank.notificationgatewayservice.dto.template.TemplateType;
import uz.tengebank.notificationgatewayservice.dto.template.batch.BatchRenderRequest;
import uz.tengebank.notificationgatewayservice.dto.template.batch.RenderResult;
import uz.tengebank.notificationgatewayservice.exception.ApiException;
import uz.tengebank.notificationgatewayservice.repository.PushTokenRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final PushTokenRepository pushTokenRepository;
  private final TemplateServiceClient templateServiceClient;
  private final NotificationDispatcher notificationDispatcher;
  private final EventPublisher eventPublisher;

  private final String KEY_SEPARATOR = "::";

  @Async("virtualThreadExecutor")
  public void processNotification(NotificationRequest payload) {
    log.info("Gateway: Started processing notification request: {}", payload.requestId());
    eventPublisher.publishRequestAcceptedEvent(payload);

    try {
      validateTemplate(payload.templateName());

      Map<String, RenderResult> resultMap = performBatchRendering(payload);
      if (resultMap == null) {
        log.error("Rendering templates failed.");
        eventPublisher.publishRequestFailedEvent(payload, "RENDER_RESULT_NULL", "Batch render operation returned no data.");
        return;
      }

      switch (payload.deliveryStrategy()) {
        case FALLBACK -> processWithFallback(payload, resultMap);
        case PARALLEL -> processInParallel(payload, resultMap);
      }
    } catch (FeignException e) {
      eventPublisher.publishRequestFailedEvent(payload, e.getMessage(), e.contentUTF8());
    } catch (Exception e) {
      log.error("A critical error occurred while processing request {}: {}", payload.requestId(), e.getMessage(), e);
      eventPublisher.publishRequestFailedEvent(payload, e.getMessage(), e.getCause().getMessage());
    }
  }

  private void processInParallel(NotificationPayload payload, Map<String, RenderResult> resultMap) {
    log.info("Processing request {} with PARALLEL strategy", payload.requestId());

    payload.recipients().forEach(recipient -> {
      if(payload.channels().contains(NotificationPayload.Channel.PUSH)) {
        final String taskKey = recipient.id() + KEY_SEPARATOR + NotificationPayload.Channel.PUSH.name();
        RenderResult pushResult = resultMap.get(taskKey);

        if(isRenderResultInvalid(pushResult, recipient)) {
          eventPublisher.publishNotificationAttemptFailedEvent(
              payload,
              recipient.id(),
              NotificationPayload.Channel.PUSH.name(),
              pushResult != null ? pushResult.error().errorCode() : "RENDER_RESULT_FAILED",
              pushResult != null ? pushResult.error().message() : "Unknown render result"
          );
          return;
        }

        var pushConfig = Optional.ofNullable(payload.channelConfig())
            .map(NotificationPayload.ChannelConfig::push)
            .orElse(null);

        dispatchPushNotification(recipient, pushConfig, pushResult.data());
      }

      if(payload.channels().contains(NotificationPayload.Channel.SMS)) {
        final String taskKey = recipient.id() + KEY_SEPARATOR + NotificationPayload.Channel.SMS.name();
        RenderResult smsResult = resultMap.get(taskKey);

        if (isRenderResultInvalid(smsResult, recipient)) {
          eventPublisher.publishNotificationAttemptFailedEvent(
              payload,
              recipient.id(),
              NotificationPayload.Channel.PUSH.name(),
              smsResult != null ? smsResult.error().errorCode() : "RENDER_RESULT_FAILED",
              smsResult != null ? smsResult.error().message() : "Unknown render result"
          );
          return;
        }

        var smsConfig = Optional.ofNullable(payload.channelConfig())
            .map(NotificationPayload.ChannelConfig::sms)
            .orElse(null);

        dispatchSmsNotification(recipient, smsConfig, smsResult.data().body());
      }
    });

  }

  private void processWithFallback(NotificationPayload payload, Map<String, RenderResult> resultMap) {
    log.info("Processing request {} with FALLBACK strategy", payload.requestId());

    for (var recipient : payload.recipients()) {
      boolean dispatched = false;
      for (var channel : payload.channels()) {
        final String taskKey = recipient.id() + KEY_SEPARATOR + channel.name();
        RenderResult result = resultMap.get(taskKey);

        if (isRenderResultInvalid(result, recipient)) {
          log.warn("Render failed for recipient {} on channel {}, trying next fallback channel.", recipient.phone(), channel);
          eventPublisher.publishNotificationAttemptFailedEvent(
              payload,
              recipient.id(),
              channel.name(),
              result != null ? result.error().errorCode() : "RENDER_RESULT_FAILED",
              result != null ? result.error().message() : "Unknown render result"
          );
          continue; // Move to the next channel.
        }

        boolean dispatchSuccess = false;

        if (channel == NotificationPayload.Channel.PUSH) {
          var pushConfig = Optional.ofNullable(payload.channelConfig())
              .map(NotificationPayload.ChannelConfig::push)
              .orElse(null);

          dispatchSuccess = dispatchPushNotification(recipient, pushConfig, result.data());
        } else if (channel == NotificationPayload.Channel.SMS) {
          var smsConfig = Optional.ofNullable(payload.channelConfig())
              .map(NotificationPayload.ChannelConfig::sms)
              .orElse(null);

          dispatchSmsNotification(recipient, smsConfig, result.data().body());
          dispatchSuccess = true;
        }

        if (dispatchSuccess) {
          log.info("Dispatched to {} via fallback channel {}", recipient.phone(), channel);
          dispatched = true;
          break;
        }
      }

      if (!dispatched) {
        log.error("All fallback channels failed for recipient {}", recipient.phone());
        eventPublisher.publishNotificationAttemptFailedEvent(
            payload,
            recipient.id(),
            Arrays.toString(NotificationPayload.Channel.values()),
            "ALL_CHANNELS_FAILED",
            "Notification could not be dispatched for recipient [" + recipient.id() + "]: " + recipient.phone()
        );
      }
    }
  }


  private void validateTemplate(final String templateName) {
    var templateResponse = templateServiceClient.getTemplateByName(templateName);
    if (templateResponse.status() == ApiResponse.Status.ERROR) {
      throw new ApiException(CommonErrorCode.TEMPLATE_SERVICE_EXCEPTION, templateResponse.error().message());
    }
    log.info("Template '{}' validated successfully.", templateName);
  }

  private Map<String, RenderResult> performBatchRendering(NotificationPayload payload) throws ApiException {
    var renderTasks = payload.recipients().stream()
        .flatMap(r -> payload.channels().stream().map(c ->
            new BatchRenderRequest.RenderTask(
                r.id() + KEY_SEPARATOR + c.name(),
                r.lang(),
                TemplateType.valueOf(c.name()),
                r.variables()
            )))
        .distinct()
        .toList();

    BatchRenderRequest batchRequest = new BatchRenderRequest(payload.templateName(), renderTasks);
    ApiResponse<List<RenderResult>> renderApiResponse = templateServiceClient.renderBatch(batchRequest);

    if (renderApiResponse.status() == ApiResponse.Status.ERROR) {
      throw new ApiException(CommonErrorCode.TEMPLATE_SERVICE_EXCEPTION, renderApiResponse.error().message());
    }

    return renderApiResponse.data().stream()
        .collect(Collectors.toMap(RenderResult::recipientId, Function.identity()));
  }

  private boolean dispatchPushNotification(
      NotificationPayload.Recipient recipient,
      Map<String, Object> pushConfig,
      RenderResult.RenderedTemplate renderedTemplate
  ) {
    // todo: pass as parameter
    List<String> tokens = pushTokenRepository.findTokensByPhone(recipient.phone());
    if (tokens.isEmpty()) {
      log.error("No push tokens found for phone {}. Push attempt failed.", recipient.phone());
      return false;
    }
    // TODO: Implement Push Data retrieval from template/variables
    var pushContent = new PushPayload.PushContent(renderedTemplate.title(), renderedTemplate.body(), renderedTemplate.imageUrl(), Map.of());
    for (String token : tokens) {
      var message = new PushPayload(token, pushContent, pushConfig);
      notificationDispatcher.dispatchPush(message);
    }
    return true;
  }

  private void dispatchSmsNotification(NotificationPayload.Recipient recipient, Map<String, Object> smsConfig, String renderedBody) {
    var message = new SmsPayload(recipient.phone(), renderedBody, smsConfig);
    notificationDispatcher.dispatchSms(message);
  }

  private boolean isRenderResultInvalid(RenderResult result, NotificationPayload.Recipient recipient) {
    if (result == null || !result.success()) {
      log.error("Skipping recipient {}: Rendering failed. Reason: {}",
          recipient.phone(), result != null ? result.error() : "Unknown render result");
      return true;
    }
    return false;
  }

  private void handleBatchFailure(NotificationPayload payload, String reason, String errorJson) {
    log.error("Aborting request {}. Reason: {}. Details: {}", payload.requestId(), reason, errorJson);

    Map<String, Object> auditPayload = Map.of(
        "requestId", payload.requestId(),
        "failureReason", reason,
        "errorDetails", errorJson
    );

    // TODO: Emit a single batch failure event to the audit queue with the reason and details.
  }

}
