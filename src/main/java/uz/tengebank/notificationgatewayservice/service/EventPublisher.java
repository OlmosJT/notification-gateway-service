package uz.tengebank.notificationgatewayservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.tengebank.notificationcontracts.dto.NotificationRequest;
import uz.tengebank.notificationcontracts.dto.enums.IndividualNotificationStatus;
import uz.tengebank.notificationcontracts.dto.enums.NotificationRequestStatus;
import uz.tengebank.notificationcontracts.events.EventEnvelope;
import uz.tengebank.notificationcontracts.events.EventFactory;
import uz.tengebank.notificationcontracts.events.EventType;
import uz.tengebank.notificationcontracts.payload.NotificationDestinationPayload;
import uz.tengebank.notificationcontracts.payload.NotificationRequestPayload;
import uz.tengebank.notificationcontracts.payload.Payload;
import uz.tengebank.notificationgatewayservice.config.props.ApplicationProperties;

import java.util.UUID;

@Slf4j
@Service
public class EventPublisher {

    private final String auditExchangeName;
    private final String applicationName;
    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(
            ApplicationProperties props,
            @Value("${spring.application.name}")
            String applicationName,
            RabbitTemplate rabbitTemplate
    ) {
        this.auditExchangeName = props.rabbitmq().exchanges().audit();
        this.applicationName = applicationName;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishRequestAcceptedEvent(NotificationRequest request) {
        var eventPayload = new NotificationRequestPayload(
                request.requestId(),
                NotificationRequestStatus.ACCEPTED,
                "Request accepted by Gateway",
                null,
                request
        );

        publish(EventType.NOTIFICATION_REQUEST_ACCEPTED_V1, eventPayload);
    }

    public void publishRequestFailedEvent(NotificationRequest request, String reason, String details) {
        var eventPayload = new NotificationRequestPayload(
                request.requestId(),
                NotificationRequestStatus.FAILED,
                reason,
                details,
                request
        );

        publish(EventType.NOTIFICATION_REQUEST_FAILED_V1, eventPayload);
    }

    public void publishNotificationAttemptFailedEvent(NotificationRequest request, UUID destinationId, String errorCode, String errorMessage) {
        var eventPayload = new NotificationDestinationPayload(
                request.requestId(),
                destinationId,
                IndividualNotificationStatus.INTERNAL_FAILURE,
                errorMessage,
                errorCode
        );
        publish(EventType.INDIVIDUAL_NOTIFICATION_INTERNAL_FAILURE_V1, eventPayload);
    }

    private void publish(String eventType, Payload payload) {
        try {
            final String routingKey = "notification.event";
            final EventEnvelope envelope = EventFactory.create(payload, eventType, applicationName);

            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

            log.info("Publishing event '{}' to exchange '{}' with CorrelationData '{}'", eventType, auditExchangeName, correlationData);

            rabbitTemplate.convertAndSend(auditExchangeName, routingKey, envelope, correlationData);

        } catch (AmqpException e) {
            log.error("Failed to publish event '{}'. Payload: {}", eventType, payload, e);
        }
    }

}
