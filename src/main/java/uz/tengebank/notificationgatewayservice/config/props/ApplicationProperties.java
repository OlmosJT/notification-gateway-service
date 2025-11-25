package uz.tengebank.notificationgatewayservice.config.props;

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
          Exchanges exchanges,
          Queues queues
  ) {}

  public record NotificationTemplateService(
          @NotBlank String url,
          BasicAuth basicAuth
  ) {
    public record BasicAuth(
        @NotBlank String username,
        @NotBlank String password
    ) {}
  }

  public record Exchanges(
      @NotBlank String internal,
      @NotBlank String audit
  ) {}

  public record Queues(
          @NotBlank String sms,
          @NotBlank String fcm,
          @NotBlank String hcm
  ) {}

}
