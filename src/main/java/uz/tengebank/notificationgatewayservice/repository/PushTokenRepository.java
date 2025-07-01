package uz.tengebank.notificationgatewayservice.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Later this repository will be separated from notification-gateway-service
 * and owns its microservice notification-registry-service
 */

@Repository
public interface PushTokenRepository {
  List<String> findTokensByPhone(String phone);
}
