package it.gov.pagopa.decoupler.util.constant;

public class ControllerConstants {

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
