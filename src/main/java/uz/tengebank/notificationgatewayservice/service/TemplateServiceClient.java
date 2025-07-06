package uz.tengebank.notificationgatewayservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uz.tengebank.notificationgatewayservice.config.FeignAuthConfig;
import uz.tengebank.notificationgatewayservice.dto.ApiResponse;
import uz.tengebank.notificationgatewayservice.dto.template.NotificationTemplateResponseDto;
import uz.tengebank.notificationgatewayservice.dto.template.batch.BatchRenderRequest;
import uz.tengebank.notificationgatewayservice.dto.template.batch.RenderResult;

import java.util.List;

@FeignClient(
    name = "notification-template-service",
    url = "${app.services.notification-template-service.url}/v1/api/templates",
    configuration = FeignAuthConfig.class
)
public interface TemplateServiceClient {

  @GetMapping("/{name}")
  ApiResponse<NotificationTemplateResponseDto> getTemplateByName(@PathVariable("name") String templateName);

  @PostMapping("/render/batch")
  ApiResponse<List<RenderResult>> renderBatch(@RequestBody BatchRenderRequest request);

}
