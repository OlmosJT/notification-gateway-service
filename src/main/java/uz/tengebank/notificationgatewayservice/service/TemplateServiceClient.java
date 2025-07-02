package uz.tengebank.notificationgatewayservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-template-service", url = "${app.services.notification-template-service.url}/v1/api/templates")
public interface TemplateServiceClient {

//  @GetMapping("/{name}")
//  ApiResponse<NotificationTemplateResponseDto> getTemplateByName(@PathVariable("name") String templateName);
//
//  @PostMapping("/render/batch")
//  ApiResponse<BatchRenderResponse> renderBatch(@RequestBody BatchRenderRequest request);

}
