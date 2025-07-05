package uz.tengebank.notificationgatewayservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import uz.tengebank.notificationgatewayservice.config.ApplicationProperties;
import uz.tengebank.notificationgatewayservice.dto.notification.PushPayload;
import uz.tengebank.notificationgatewayservice.dto.notification.SmsPayload;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQNotificationDispatcher implements NotificationDispatcher {

    private final RabbitTemplate rabbitTemplate;
    private final ApplicationProperties props;


    @Override
    public void dispatchSms(SmsPayload message) {
        final String SMS_QUEUE = props.rabbitmq().queues().sms();
        log.info("Dispatching SMS to queue '{}' for target {}", SMS_QUEUE, message.phone());
        rabbitTemplate.convertAndSend(SMS_QUEUE, message);
    }

    @Override
    public void dispatchPush(PushPayload message) {
        final String FCM_QUEUE = props.rabbitmq().queues().fcm();
        log.info("Dispatching Push to queue '{}' for target {}", FCM_QUEUE, message.token());
        rabbitTemplate.convertAndSend(FCM_QUEUE, message);
    }
}
