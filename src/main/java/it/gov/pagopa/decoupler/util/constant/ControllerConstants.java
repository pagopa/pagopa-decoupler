package it.gov.pagopa.decoupler.util.constant;

public class ControllerConstants {

  public static final String OPENAPI_BADREQUEST_EXAMPLE =
      """
      {
        "httpStatusCode": 400,
        "httpStatusDescription": "Bad Request",
        "appErrorCode": "DECP-XXXX",
        "errors": [
          {
            "path": "<detail.path.if-exist>",
            "message": "<detail.message>"
          }
        ]
      }\
      """;

  public static final String OPENAPI_NOTFOUND_EXAMPLE =
      """
      {
        "httpStatusCode": 404,
        "httpStatusDescription": "Not Found",
        "appErrorCode": "DECP-XXXX",
        "errors": [
          {
            "message": "<detail.message>"
          }
        ]
      }\
      """;

  public static final String OPENAPI_INTERNALSERVERERROR_EXAMPLE =
      """
        {
        "errorId": "50905466-1881-457b-b42f-fb7b2bfb1610",
        "httpStatusCode": 500,
        "httpStatusDescription": "Internal Server Error",
        "appErrorCode": "DECP-0500",
        "errors": [
          {
            "message": "An unexpected error has occurred. Please contact support."
          }
        ]
      }\
      """;

  private ControllerConstants() {}
}
