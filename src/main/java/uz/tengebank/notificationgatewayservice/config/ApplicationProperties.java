package uz.tengebank.notificationgatewayservice.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record ApplicationProperties(
        Services services,
        RabbitMQ rabbitmq
) {

  public record Services(
          NotificationTemplateService notificationTemplateService
  ) {}

  public record RabbitMQ(
          Queues queues
  ) {}

  public record NotificationTemplateService(
          @NotBlank String url
  ) {}

  public record Queues(
          @NotBlank String sms,
          @NotBlank String fcm,
          @NotBlank String hcm,
          @NotBlank String event
  ) {}

}
