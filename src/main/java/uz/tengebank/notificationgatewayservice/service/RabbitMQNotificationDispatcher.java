package uz.tengebank.notificationgatewayservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import uz.tengebank.notificationgatewayservice.config.ApplicationProperties;
import uz.tengebank.notificationgatewayservice.dto.notification.PushPayload;
import uz.tengebank.notificationgatewayservice.dto.notification.SmsPayload;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQNotificationDispatcher implements NotificationDispatcher {

    private final RabbitTemplate rabbitTemplate;
    private final ApplicationProperties props;


    @Override
    public void dispatchSms(SmsPayload message) {
        final String exchangeName = props.rabbitmq().exchanges().direct();
        final String routingKey = "notification.sms";

        log.info("Dispatching SMS to exchange '{}' with routing key '{}' for target {}",
            exchangeName, routingKey, message.phone());

        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend(exchangeName, routingKey, message, correlationData);
        log.info("SMS message sent with correlation ID: {}", correlationData.getId());
    }

    @Override
    public void dispatchPush(PushPayload message) {
        final String exchangeName = props.rabbitmq().exchanges().direct();
        final String routingKey = "notification.fcm";

        log.info("Dispatching Push to exchange '{}' with routing key '{}' for target {}",
            exchangeName, routingKey, message.token());

        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend(exchangeName, routingKey, message, correlationData);
        log.info("Push message sent with correlation ID: {}", correlationData.getId());

    }
}
