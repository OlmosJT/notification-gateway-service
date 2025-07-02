package uz.tengebank.notificationgatewayservice.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import uz.tengebank.notificationgatewayservice.exception.ErrorCode;

import java.util.List;

public record ApiResponse<T> (
    Integer code,
    Status status,
    T data,
    ApiError error
) {

    public enum Status {
        SUCCESS, ERROR;

        @JsonValue
        public String toLowerCase() {
            return this.toString().toLowerCase();
        }
    }

    // --- Static Factory Methods ---

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, Status.SUCCESS, data, null);
    }

    public static <T> ApiResponse<T> error(
            ErrorCode errorCode,
            String message,
            List<ApiError.ApiValidationError> details
    ) {
        return new ApiResponse<>(
                errorCode.getCode(),
                Status.ERROR,
                null,
                new ApiError(errorCode.getStringCode(), message, details));
    }
}
