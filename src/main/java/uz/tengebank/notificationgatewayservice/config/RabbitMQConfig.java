package uz.tengebank.notificationgatewayservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uz.tengebank.notificationgatewayservice.config.props.ApplicationProperties;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {
    private final ApplicationProperties props;

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
        rabbitTemplate.setMandatory(true);

        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("✅ Message confirmed: ID={}", correlationData != null ? correlationData.getId() : "null");
            } else {
                log.error("❌ Message NACK: Cause={} | ID={}", cause, correlationData != null ? correlationData.getId() : "null");
            }
        });

        rabbitTemplate.setReturnsCallback(returned ->
                log.error("🔁 Returned: exchange={}, routingKey={}, replyCode={}, text={}",
                        returned.getExchange(), returned.getRoutingKey(),
                        returned.getReplyCode(), returned.getReplyText())
        );

        return rabbitTemplate;
    }

    // --- 1. Internal Gateway Exchange (Direct) ---

    @Bean
    public DirectExchange internalExchange() {
        return new DirectExchange(props.rabbitmq().exchanges().internal(), true, false);
    }

    @Bean public Queue smsQueue() { return new Queue(props.rabbitmq().queues().sms(), true); }
    @Bean public Queue fcmQueue() { return new Queue(props.rabbitmq().queues().fcm(), true); }
    @Bean public Queue hcmQueue() { return new Queue(props.rabbitmq().queues().hcm(), true); }

    @Bean
    public Binding smsBinding(Queue smsQueue, DirectExchange internalExchange) {
        return BindingBuilder.bind(smsQueue).to(internalExchange).with("notification.sms");
    }

    @Bean
    public Binding fcmBinding(Queue fcmQueue, DirectExchange internalExchange) {
        return BindingBuilder.bind(fcmQueue).to(internalExchange).with("notification.fcm");
    }

    @Bean
    public Binding hcmBinding(Queue hcmQueue, DirectExchange internalExchange) {
        return BindingBuilder.bind(hcmQueue).to(internalExchange).with("notification.hcm");
    }

    // --- 2. Audit Service Exchange (Custom/Delayed) ---
    @Bean
    public CustomExchange auditExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "topic");
        return new CustomExchange(
                props.rabbitmq().exchanges().audit(),
                "x-delayed-message",
                true,
                false,
                args
        );
    }
}
