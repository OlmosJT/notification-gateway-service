package uz.tengebank.notificationgatewayservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.tengebank.notificationcontracts.events.EventEnvelope;
import uz.tengebank.notificationcontracts.events.EventFactory;
import uz.tengebank.notificationcontracts.events.EventType;
import uz.tengebank.notificationcontracts.payload.NotificationRequestAccepted;
import uz.tengebank.notificationcontracts.payload.Payload;
import uz.tengebank.notificationgatewayservice.config.ApplicationProperties;
import uz.tengebank.notificationgatewayservice.dto.notification.NotificationPayload;

import java.util.UUID;

@Slf4j
@Service
public class EventPublisher {

  private final String exchangeName;
  private final String applicationName;
  private final RabbitTemplate rabbitTemplate;

  public EventPublisher(
      ApplicationProperties props,
      @Value("${spring.application.name}")
      String applicationName,
      RabbitTemplate rabbitTemplate
  ) {
    this.exchangeName = props.rabbitmq().exchanges().direct();

    this.applicationName = applicationName;
    this.rabbitTemplate = rabbitTemplate;
  }

  public void publish(String eventType, Payload payload) {
    try {
      final String routingKey = "notification.event";
      final EventEnvelope envelope = EventFactory.create(payload, eventType, applicationName);

      log.info("Sending event to exchange '{}' with routing key '{}'. EventEnvelope: {}", exchangeName, routingKey, envelope);

      CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
      rabbitTemplate.convertAndSend(exchangeName, routingKey, envelope, correlationData);
      log.info("Emit envelope sent with correlation ID: {}", correlationData.getId());

    } catch (AmqpException e) {
      log.error("Failed to publish event '{}'. Payload: {}", eventType, payload, e);
    }
  }

  public void publishRequestAcceptedEvent(NotificationPayload payload) {
    var recipients = payload.recipients().stream()
        .map(recipient -> new NotificationRequestAccepted.Recipient(
            recipient.phone(),
            recipient.lang(),
            recipient.variables())
        ).toList();

    var channelConfig = payload.channelConfig() != null
        ? new NotificationRequestAccepted.ChannelConfig(payload.channelConfig().sms(), payload.channelConfig().push())
        : null;

    var eventPayload = new NotificationRequestAccepted(
        payload.requestId(),
        payload.source(),
        payload.category(),
        payload.templateName(),
        payload.channels().stream().map(Enum::name).toList(),
        payload.deliveryStrategy().name(),
        channelConfig,
        recipients
    );

    publish(EventType.NOTIFICATION_REQUEST_ACCEPTED_V1, eventPayload);
  }

}
