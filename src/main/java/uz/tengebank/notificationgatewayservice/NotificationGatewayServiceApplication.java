package uz.tengebank.notificationgatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import uz.tengebank.notificationgatewayservice.config.ApplicationProperties;


@EnableAsync
@EnableFeignClients
@EnableConfigurationProperties(ApplicationProperties.class)
@SpringBootApplication
public class NotificationGatewayServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(NotificationGatewayServiceApplication.class, args);
  }

}
