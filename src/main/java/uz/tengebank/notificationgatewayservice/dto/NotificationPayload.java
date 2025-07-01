package uz.tengebank.notificationgatewayservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record NotificationPayload(
        @NotNull(message = "requestId cannot be null")
        UUID requestId,
        @NotBlank(message = "source cannot be blank")
        String source,
        @NotBlank(message = "category cannot be blank")
        String category,
        @NotBlank(message = "templateName cannot be blank")
        String templateName,
        @NotEmpty(message = "channels list cannot be empty")
        List<Channel> channels,
        @NotNull(message = "deliveryStrategy cannot be null")
        DeliveryStrategy deliveryStrategy,
        @NotNull(message = "channelConfig cannot be null")
        ChannelConfig channelConfig,
        @Valid
        @NotEmpty(message = "recipients list cannot be empty")
        @Size(min = 1, max = 50, message = "The number of recipients cannot exceed 50 per request")
        List<Recipient> recipients
) {

  public enum Channel {
    PUSH, SMS;

    @JsonCreator
    public static Channel fromString(String value) {
      if (value == null) {
        return null;
      }

      try {
        return Channel.valueOf(value.toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
                "Invalid channel type: '" + value + "'. Accepted values are 'sms' or 'push'."
        );
      }
    }
  }

  public enum DeliveryStrategy {
    PARALLEL, FALLBACK;

    @JsonCreator
    public static DeliveryStrategy fromString(String value) {
      if (value == null) {
        return null;
      }

      try {
        return DeliveryStrategy.valueOf(value.toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
                "Invalid delivery strategy type: '" + value + "'. Accepted values are 'parallel' or 'fallback'."
        );
      }
    }
  }

  public record ChannelConfig(
          Map<String, Object> sms,
          Map<String, Object> push
  ) {}

  public record Recipient(
          @NotBlank(message = "phone cannot be blank")
          @Pattern(regexp = "\\d{12}", message = "Phone must be a 12-digit number (e.g., 998931234567)")
          String phone,
          Map<String, Object> variables,
          @NotBlank(message = "lang cannot be blank")
          @Pattern(regexp = "uz|en|ru", flags = Pattern.Flag.CASE_INSENSITIVE, message = "lang must be 'uz', 'en', or 'ru'")
          String lang
  ) {}
}
