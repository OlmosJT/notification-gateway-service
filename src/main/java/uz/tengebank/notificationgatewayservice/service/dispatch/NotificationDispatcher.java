package uz.tengebank.notificationgatewayservice.service.dispatch;

import uz.tengebank.notificationgatewayservice.dto.notification.PushPayload;
import uz.tengebank.notificationgatewayservice.dto.notification.SmsPayload;

public interface NotificationDispatcher {
    void dispatchSms(SmsPayload message);
    void dispatchPush(PushPayload message);
}
