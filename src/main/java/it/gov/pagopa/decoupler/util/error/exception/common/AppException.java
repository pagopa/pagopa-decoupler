package it.gov.pagopa.decoupler.util.error.exception.common;

import it.gov.pagopa.decoupler.util.error.enums.AppErrorCodeMessageEnum;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

  private final transient AppErrorCodeMessageEnum codeMessage;

  private final transient Object[] args;

  private final transient Object path;

  public AppException(Throwable cause, AppErrorCodeMessageEnum codeMessage) {
    super(cause);
    this.codeMessage = codeMessage;
    this.args = null;
    this.path = null;
  }

  public AppException(Throwable cause, AppErrorCodeMessageEnum codeMessage, Object... args) {
    super(cause);
    this.codeMessage = codeMessage;
    this.args = args;
    this.path = null;
  }

  public AppException(AppErrorCodeMessageEnum codeMessage) {
    super();
    this.codeMessage = codeMessage;
    this.args = null;
    this.path = null;
  }

  public AppException(AppErrorCodeMessageEnum codeMessage, Serializable... args) {
    super();
    this.codeMessage = codeMessage;
    this.args = args;
    this.path = null;
  }

  public AppException(AppErrorCodeMessageEnum codeMessage, Object path, Serializable... args) {
    super();
    this.codeMessage = codeMessage;
    this.args = args;
    this.path = path;
  }
}
