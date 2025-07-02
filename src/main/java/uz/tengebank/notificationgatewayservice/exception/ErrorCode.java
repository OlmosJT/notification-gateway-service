package uz.tengebank.notificationgatewayservice.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    int getCode();
    String getStringCode();
    String getMessage();
    HttpStatus getHttpStatus();

    ErrorCodeGroup getGroup();

    default boolean isClientError() {
        return getHttpStatus().is4xxClientError();
    }

    default boolean isServerError() {
        return getHttpStatus().is5xxServerError();
    }
}
