package uz.tengebank.notificationgatewayservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uz.tengebank.notificationgatewayservice.config.ApplicationProperties;
import uz.tengebank.notificationgatewayservice.dto.NotificationPayload;
import uz.tengebank.notificationgatewayservice.repository.PushTokenRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final RabbitTemplate rabbitTemplate;
  private final PushTokenRepository pushTokenRepository;
  private final TemplateServiceClient templateServiceClient;
  private final ApplicationProperties props;

  @Async("virtualThreadExecutor")
  public void processNotification(NotificationPayload payload) {
    log.info("Gateway: Started processing notification request: {}", payload);

  }



  private boolean trySendSms(NotificationPayload.Recipient recipient, Map<String, Object> smsConfig, String renderedText) {
    String smsQueue = props.rabbitmq().queues().sms();
    log.info("Dispatching SMS for phone {} to queue '{}'.", recipient.phone(), smsQueue);

    Map<String, Object> messagePayload = Map.of("target", recipient.phone(), "content", renderedText, "config", smsConfig);
    rabbitTemplate.convertAndSend(smsQueue, messagePayload);
    return true;
  }

  private boolean trySendPush(NotificationPayload.Recipient recipient, Map<String, Object> pushConfig, String renderedText) {
    List<String> tokens = pushTokenRepository.findTokensByPhone(recipient.phone());
    if (tokens.isEmpty()) {
      log.warn("No push tokens found for phone {}. Push attempt failed.", recipient.phone());
      return false;
    }

    String fcmQueue = props.rabbitmq().queues().fcm();
    log.info("Found {} token(s) for phone {}. Dispatching to queue '{}'.", tokens.size(), recipient.phone(), fcmQueue);
    for (String token : tokens) {
      Map<String, Object> messagePayload = Map.of("target", token, "content", renderedText, "config", pushConfig);
      rabbitTemplate.convertAndSend(fcmQueue, messagePayload);
    }
    return true;
  }
}
