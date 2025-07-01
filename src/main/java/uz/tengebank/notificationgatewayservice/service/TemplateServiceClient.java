package uz.tengebank.notificationgatewayservice.service;

import org.springframework.stereotype.Service;

@Service
public interface TemplateServiceClient {
  boolean templateExists(String templateName);

  Map<String, RenderResult> renderBatch(String templateName, List<RenderRequest> requests);
}
