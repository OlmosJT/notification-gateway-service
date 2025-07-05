package uz.tengebank.notificationgatewayservice.constants;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import uz.tengebank.notificationgatewayservice.exception.ErrorCode;
import uz.tengebank.notificationgatewayservice.exception.ErrorCodeGroup;

@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {
  TEMPLATE_SERVICE_EXCEPTION(2000, "TEMPLATE_SERVICE_EXCEPTION", "Template not found.", HttpStatus.NOT_FOUND),
  INTERNAL_SERVER_ERROR(1500, "INTERNAL_SERVER_ERROR", "An unexpected internal server error occurred.", HttpStatus.INTERNAL_SERVER_ERROR)
  ;

  private final int code;
  private final String stringCode;
  private final String message;
  private final HttpStatus httpStatus;

  @Override public int getCode() { return code; }
  @Override public String getStringCode() { return stringCode; }
  @Override public String getMessage() { return message; }
  @Override public HttpStatus getHttpStatus() { return httpStatus; }
  @Override public ErrorCodeGroup getGroup() { return ErrorCodeGroup.COMMON; }
}
