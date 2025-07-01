package uz.tengebank.notificationgatewayservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  @Value("${app.rabbitmq.queues.sms}")
  private String smsQueueName;

  @Value("${app.rabbitmq.queues.fcm}")
  private String fcmQueueName;

  @Value("${app.rabbitmq.queues.hcm}")
  private String hcmQueueName;

  @Value("${app.rabbitmq.queues.events}")
  private String eventsQueueName;

  @Bean
  public Queue smsQueue() {
    return new Queue(smsQueueName, true);
  }

  @Bean
  public Queue fcmQueue() {
    return new Queue(fcmQueueName, true);
  }

  @Bean
  public Queue hcmQueue() {
    return new Queue(hcmQueueName, true);
  }

  @Bean
  public Queue eventsQueue() {
    return new Queue(eventsQueueName, true);
  }
}
