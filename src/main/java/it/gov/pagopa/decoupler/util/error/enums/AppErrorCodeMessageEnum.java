package it.gov.pagopa.decoupler.util.error.enums;

import it.gov.pagopa.decoupler.util.constant.AppConstant;
import it.gov.pagopa.decoupler.util.logging.AppMessageUtil;
import org.jboss.resteasy.reactive.RestResponse;

public enum AppErrorCodeMessageEnum {
  ERROR(
      "0500",
      "system.error",
      RestResponse.Status.INTERNAL_SERVER_ERROR,
      "An error occurred during computation. This could be caused by an applicative error and it is"
          + " probably required to open an issue.");

  private final String errorCode;
  private final String errorMessageKey;
  private final RestResponse.Status httpStatus;
  private final String openAPIDescription;

  AppErrorCodeMessageEnum(
      String errorCode,
      String errorMessageKey,
      RestResponse.Status httpStatus,
      String openAPIDescription) {

    this.errorCode = errorCode;
    this.errorMessageKey = errorMessageKey;
    this.httpStatus = httpStatus;
    this.openAPIDescription = openAPIDescription;
  }

  public String errorCode() {
    return AppConstant.SERVICE_CODE_APP + "-" + errorCode;
  }

  public String message(Object... args) {
    return AppMessageUtil.getMessage(errorMessageKey, args);
  }

  public RestResponse.Status httpStatus() {
    return httpStatus;
  }

  public String openAPIDescription() {
    return this.openAPIDescription;
  }
}
