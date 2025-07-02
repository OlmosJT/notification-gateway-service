package uz.tengebank.notificationgatewayservice.dto;


import java.util.List;

public record ApiError(
        String errorCode,
        String message,
        List<ApiValidationError> details
) {

    public ApiError(String errorCode, String message) {
        this(errorCode, message, null);
    }

    public record ApiValidationError(
            String field,
            String message
    ) {}
}
