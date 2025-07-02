package uz.tengebank.notificationgatewayservice.dto.template;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TemplateType {
  SMS, PUSH;

  @JsonCreator
  public static TemplateType fromString(String value) {
    if (value == null) {
      return null;
    }

    try {
      return TemplateType.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
              "Invalid template type: '" + value + "'. Accepted values are 'sms' or 'push'."
      );
    }
  }
}
