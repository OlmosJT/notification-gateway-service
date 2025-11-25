package uz.tengebank.notificationgatewayservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.tengebank.notificationcontracts.dto.NotificationRequest;
import uz.tengebank.notificationgatewayservice.service.NotificationService;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @PostMapping("/send")
  public ResponseEntity<Void> sendNotification(@Valid @RequestBody NotificationRequest payload) {
    notificationService.processNotification(payload);
    return ResponseEntity.accepted().build();
  }

}
