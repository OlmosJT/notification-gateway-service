package uz.tengebank.notificationgatewayservice.config;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@RequiredArgsConstructor
public class FeignAuthConfig {

    private final ApplicationProperties props;

  @Bean
  public RequestInterceptor basicAuthRequestInterceptor() {
    return requestTemplate -> {
      String auth = props.services().notificationTemplateService().basicAuth().username() + ":" + props.services().notificationTemplateService().basicAuth().password();
      String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
      requestTemplate.header("Authorization", "Basic " + encodedAuth);
    };
  }
}
