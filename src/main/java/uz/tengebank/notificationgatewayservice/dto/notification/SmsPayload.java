package uz.tengebank.notificationgatewayservice.dto.notification;

import java.util.Map;

public record SmsPayload(
    String phone,
    String content,
    Map<String, Object> config
) {
}
