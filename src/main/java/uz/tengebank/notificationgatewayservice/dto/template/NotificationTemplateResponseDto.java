package uz.tengebank.notificationgatewayservice.dto.template;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

public record NotificationTemplateResponseDto(
        Long id,
        String name,
        SmsTemplateDto smsTemplate,
        PushTemplateDto pushTemplate,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate,
        String createdBy,
        String lastModifiedBy,
        Long version
) {
  public record SmsTemplateDto(
          Map<String, String> content
  ) {
  }

  public record PushTemplateDto(
          Map<String, String> title,
          @NotNull(message = "Push template body cannot be null")
          Map<String, String> body,
          Map<String, String> imageUrl
  ) {
  }
}
