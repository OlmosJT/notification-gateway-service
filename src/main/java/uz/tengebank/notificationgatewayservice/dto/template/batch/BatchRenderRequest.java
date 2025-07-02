package uz.tengebank.notificationgatewayservice.dto.template.batch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import uz.tengebank.notificationgatewayservice.dto.template.TemplateType;

import java.util.List;
import java.util.Map;

public record BatchRenderRequest(
        @NotBlank String templateName,
        @NotEmpty List<RenderTask> tasks
) {

  public record RenderTask(
          @NotBlank String recipientId,
          @NotBlank String lang,
          @NotNull TemplateType type,
          @NotEmpty Map<String, Object> variables
  ) {}
}
