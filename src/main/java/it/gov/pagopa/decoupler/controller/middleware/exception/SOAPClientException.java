package it.gov.pagopa.decoupler.controller.middleware.exception;

public class SOAPClientException extends RuntimeException {

  public SOAPClientException(String message) {
    super(message);
  }

  public SOAPClientException(String message, Throwable e) {
    super(message, e);
  }
}
