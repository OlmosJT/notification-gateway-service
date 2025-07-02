package uz.tengebank.notificationgatewayservice.dto.template.batch;


import uz.tengebank.notificationgatewayservice.dto.ApiError;

public record RenderResult(
        String recipientId,
        Boolean success,
        RenderedTemplate data,
        ApiError error
) {

  public record RenderedTemplate(
          String title,
          String body,
          String imageUrl
  ) {}

  public static RenderResult success(String recipientId, RenderedTemplate data) {
    return new RenderResult(recipientId, true, data, null);
  }

  public static RenderResult error(String recipientId, ApiError error) {
    return new RenderResult(recipientId, false, null, error);
  }

}
