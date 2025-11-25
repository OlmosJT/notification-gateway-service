package uz.tengebank.notificationgatewayservice.dto.notification;

import java.util.Map;
import java.util.UUID;

public record SmsPayload(
        UUID requestId,
        UUID destinationId,
        String phone,
        String content,
        Map<String, Object> config
) {
}
