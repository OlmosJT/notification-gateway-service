package uz.tengebank.notificationgatewayservice.dto.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Main request payload for sending notifications. Single request can carry 1-50 recipients.")
public record NotificationPayload(

    @NotNull(message = "requestId cannot be null")
    @Schema(description = "A unique UUID for tracking the entire batch request.")
    UUID requestId,

    @NotBlank(message = "source cannot be blank")
    @Schema(
        description = "The client service initiating the request.",
        example = "Tenge24",
        allowableValues = {"Tenge24", "TengeBusiness"}
    )
    String source,

    @NotBlank(message = "category cannot be blank")
    @Schema(
        description = "The business category of the notification.",
        example = "security",
        allowableValues = {"offers", "security", "loans", "deposits", "accounts", "news"}
    )
    String category,

    @NotBlank(message = "templateName cannot be blank")
    @Schema(description = "The name of the template to use. Must match an existing template.", example = "COMMON_OTP_CODE")
    String templateName,

    @NotEmpty(message = "channels list cannot be empty")
    @Schema(description = "A list of channels to use for sending the notification.")
    List<Channel> channels,

    @NotNull(message = "deliveryStrategy cannot be null")
    @Schema(
        description = "The strategy for sending notifications if multiple channels are provided.",
        defaultValue = "fallback",
        allowableValues = {"fallback", "parallel"}
    )
    DeliveryStrategy deliveryStrategy,

    @Schema(description = "Optional provider-specific configurations for each channel (e.g., ttl, priority).")
    ChannelConfig channelConfig,

    @Valid
    @NotEmpty(message = "recipients list cannot be empty")
    @Size(min = 1, max = 50, message = "The number of recipients cannot exceed 50 per request")
    @Schema(description = "A list of recipients for the notification.")
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
      @Schema(description = "Key-value parameters for the SMS channel.", example = "{\"priority\": \"high\"}")
      Map<String, Object> sms,
      @Schema(description = "Key-value parameters for the PUSH channel.", example = "{\"ttl\": 3600, \"priority\": \"high\"}")
      Map<String, Object> push
  ) {
  }

  public record Recipient(

      @NotNull(message = "recipient id cannot be null")
      @Schema(description = "A unique UUID for tracking the singe recipient request in batch notification request.")
      UUID id,

      @NotBlank(message = "phone cannot be blank")
      @Pattern(regexp = "\\d{12}", message = "Phone must be a 12-digit number (e.g., 998931234567)")
      @Schema(description = "Recipient's phone number in 12-digit international format.", example = "998930082417")
      String phone,

      @Schema(description = "Key-value pairs for replacing placeholders in the template.", example = "{\"otpCode\": \"123456\"}")
      Map<String, Object> variables,

      @NotBlank(message = "lang cannot be blank")
      @Pattern(regexp = "uz|en|ru", flags = Pattern.Flag.CASE_INSENSITIVE, message = "lang must be 'uz', 'en', or 'ru'")
      @Schema(description = "Language for rendering the template.", example = "uz", allowableValues = {"uz", "en", "ru"})
      String lang
  ) {
  }
}
