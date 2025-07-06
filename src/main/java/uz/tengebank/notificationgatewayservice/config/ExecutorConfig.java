package uz.tengebank.notificationgatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


@Configuration
public class ExecutorConfig {

  @Bean(name = "virtualThreadExecutor")
  public Executor virtualThreadExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }
}
