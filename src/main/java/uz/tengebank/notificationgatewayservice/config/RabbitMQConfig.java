package uz.tengebank.notificationgatewayservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
      if (ack) {
        log.info("✅ Message confirmed by broker: Correlation ID = {}",
            correlationData != null ? correlationData.getId() : "null");
      } else {
        log.error("❌ Message NOT confirmed. Cause: {} | Correlation ID: {}",
            cause, correlationData != null ? correlationData.getId() : "null");
        // Optional: Persist or retry here
      }
    });

    rabbitTemplate.setReturnsCallback(returned -> {
      log.error("🔁 Returned Message: exchange={}, routingKey={}, replyCode={}, replyText={}",
          returned.getExchange(), returned.getRoutingKey(),
          returned.getReplyCode(), returned.getReplyText());
      // Optional: Save message to DB / Retry
    });

    rabbitTemplate.setMandatory(true);
    return rabbitTemplate;
  }

  @Bean
  public DirectExchange notificationExchange() {
    return new DirectExchange(props.rabbitmq().exchanges().direct(), true, false);
  }

  @Bean public Queue smsQueue()    { return new Queue(props.rabbitmq().queues().sms(), true); }
  @Bean public Queue fcmQueue()    { return new Queue(props.rabbitmq().queues().fcm(), true); }
  @Bean public Queue hcmQueue()    { return new Queue(props.rabbitmq().queues().hcm(), true); }
  @Bean public Queue eventQueue()  { return new Queue(props.rabbitmq().queues().event(), true); }

  @Bean
  public Binding smsBinding(Queue smsQueue, DirectExchange notificationExchange) {
    return BindingBuilder.bind(smsQueue)
        .to(notificationExchange)
        .with("notification.sms");
  }

  @Bean
  public Binding fcmBinding(Queue fcmQueue, DirectExchange notificationExchange) {
    return BindingBuilder.bind(fcmQueue)
        .to(notificationExchange)
        .with("notification.fcm");
  }

  @Bean
  public Binding hcmBinding(Queue hcmQueue, DirectExchange notificationExchange) {
    return BindingBuilder.bind(hcmQueue)
        .to(notificationExchange)
        .with("notification.hcm");
  }

  @Bean
  public Binding eventBinding(Queue eventQueue, DirectExchange notificationExchange) {
    return BindingBuilder.bind(eventQueue)
        .to(notificationExchange)
        .with("notification.event");
  }

}
