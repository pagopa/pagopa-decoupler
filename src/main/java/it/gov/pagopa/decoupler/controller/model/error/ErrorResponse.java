package it.gov.pagopa.decoupler.controller.model.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@Jacksonized
@JsonPropertyOrder({"errorId", "httpStatusCode", "httpStatusDescription", "appErrorCode", "errors"})
@RegisterForReflection
public class ErrorResponse {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Schema(
      example = "50905466-1881-457b-b42f-fb7b2bfb1610",
      description =
          "The unique identifier that can be used for tracking request and error response.")
  private String errorId;

  @Schema(example = "500", description = "The HTTP status code related to the error message.")
  private int httpStatusCode;

  @Schema(
      example = "Internal Server Error",
      description = "The descriptive name of the HTTP status code.")
  private String httpStatusDescription;

  @Schema(
      example = "DEC-0500",
      description = "The operational error code related to the error response")
  private String appErrorCode;

  @Schema(description = "The list of specific errors to show for the error response.")
  private List<ErrorMessage> errors;
}
