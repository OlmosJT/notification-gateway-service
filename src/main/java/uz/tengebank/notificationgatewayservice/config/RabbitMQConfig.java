package uz.tengebank.notificationgatewayservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {

  private final ApplicationProperties.RabbitMQ rabbitMq;

  public RabbitMQConfig(ApplicationProperties props) {
    this.rabbitMq = props.rabbitmq();
  }


  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

    rabbitTemplate.setMessageConverter(messageConverter);

    // Set up the callback for publisher confirms
    rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
      if (ack) {
        log.info("Message confirmed by broker. Correlation ID: {}", correlationData != null ? correlationData.getId() : "N/A");
      } else {
        log.error("Message NACKed by broker. Cause: {}. Correlation ID: {}", cause, correlationData != null ? correlationData.getId() : "N/A");
        // TODO: Here you could trigger a fallback, an alert, or save the failed message to a database.
      }
    });

    return rabbitTemplate;
  }

  @Bean
  public Jackson2JsonMessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }


  @Bean
  public Queue smsQueue() {
    return new Queue(rabbitMq.queues().sms(), true);
  }

  @Bean
  public Queue fcmQueue() {
    return new Queue(rabbitMq.queues().fcm(), true);
  }

  @Bean
  public Queue hcmQueue() {
    return new Queue(rabbitMq.queues().hcm(), true);
  }

  @Bean
  public Queue eventsQueue() {
    return new Queue(rabbitMq.queues().event(), true);
  }

}
