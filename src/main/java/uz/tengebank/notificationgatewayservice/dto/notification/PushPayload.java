package uz.tengebank.notificationgatewayservice.dto.notification;

import java.util.Map;
import java.util.UUID;

public record PushPayload(
        UUID requestId,
        UUID destinationId,
        String token,
        PushContent content,
        Map<String, Object> config
) {

    public record PushContent(
            String title,
            String body,
            String imageUrl,
            Map<String, String> data
    ) {
    }
}
