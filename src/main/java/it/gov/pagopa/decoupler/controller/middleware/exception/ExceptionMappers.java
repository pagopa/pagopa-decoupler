package it.gov.pagopa.decoupler.controller.middleware.exception;

import static it.gov.pagopa.decoupler.util.logging.AppMessageUtil.logErrorMessage;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import io.quarkus.arc.ArcUndeclaredThrowableException;
import it.gov.pagopa.decoupler.controller.model.error.ErrorMessage;
import it.gov.pagopa.decoupler.controller.model.error.ErrorResponse;
import it.gov.pagopa.decoupler.util.error.enums.AppErrorCodeMessageEnum;
import it.gov.pagopa.decoupler.util.error.exception.common.AppException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Objects;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMappers {

  private final Logger log;

  public ExceptionMappers(Logger log) {
    this.log = log;
  }

  @ServerExceptionMapper
  public Response mapWebApplicationException(WebApplicationException webApplicationException) {
    if (webApplicationException.getCause() instanceof JsonMappingException jsonMappingException) {
      return mapJsonMappingException(jsonMappingException).toResponse();
    } else if (webApplicationException.getCause()
        instanceof JsonParseException jsonParseException) {
      return mapJsonParseException(jsonParseException).toResponse();
    }

    return webApplicationException.getResponse();
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapAppException(AppException appEx) {
    AppErrorCodeMessageEnum codeMessage = appEx.getCodeMessage();
    RestResponse.Status status = codeMessage.httpStatus();
    String message = codeMessage.message(appEx.getArgs());
    Object path = appEx.getPath();

    ErrorMessage errorMessage =
        ErrorMessage.builder()
            .path(Objects.nonNull(path) ? path.toString() : null)
            .message(message)
            .build();

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .httpStatusCode(status.getStatusCode())
            .httpStatusDescription(status.getReasonPhrase())
            .appErrorCode(codeMessage.errorCode())
            .errors(List.of(errorMessage))
            .build();

    return RestResponse.status(codeMessage.httpStatus(), errorResponse);
  }

  private RestResponse<ErrorResponse> mapJsonMappingException(JsonMappingException exception) {
    return mapThrowable(exception);
  }

  private RestResponse<ErrorResponse> mapJsonParseException(JsonParseException exception) {
    return mapThrowable(exception);
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapInvalidFormatException(InvalidFormatException exception) {
    return mapThrowable(exception);
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapMismatchedInputException(
      MismatchedInputException exception) {
    return mapThrowable(exception);
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapUnexpectedTypeException(UnexpectedTypeException exception) {
    return mapThrowable(exception);
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapArcUndeclaredThrowableException(
      ArcUndeclaredThrowableException exception) {
    return mapThrowable(exception);
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapThrowable(Throwable exception) {

    log.errorf(logErrorMessage(exception.getMessage()));
    AppException appEx = new AppException(exception, AppErrorCodeMessageEnum.ERROR);
    AppErrorCodeMessageEnum codeMessage = appEx.getCodeMessage();
    RestResponse.Status status = codeMessage.httpStatus();

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .httpStatusCode(status.getStatusCode())
            .httpStatusDescription(status.getReasonPhrase())
            .appErrorCode(codeMessage.errorCode())
            .errors(
                List.of(
                    ErrorMessage.builder().message(codeMessage.message(appEx.getArgs())).build()))
            .build();

    return RestResponse.status(codeMessage.httpStatus(), errorResponse);
  }

  @ServerExceptionMapper
  public RestResponse<ErrorResponse> mapConstraintViolationException(
      ConstraintViolationException exception) {
    return mapThrowable(exception);
  }
}
