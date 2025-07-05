package uz.tengebank.notificationgatewayservice.dto.notification;

import java.util.Map;

public record PushPayload(
        String token,
        PushContent content,
        Map<String, Object> config
) {

  public record PushContent(
      String title,
      String body,
      String imageUrl,
      Map<String, String> data
  ) {}
}
