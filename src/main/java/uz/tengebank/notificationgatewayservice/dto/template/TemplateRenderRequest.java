package uz.tengebank.notificationgatewayservice.dto.template;

import java.util.Map;

public record TemplateRenderRequest(
        String templateName,
        Map<String, Object> variables
) {
}
