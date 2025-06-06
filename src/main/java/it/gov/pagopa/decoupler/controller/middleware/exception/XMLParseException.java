package it.gov.pagopa.decoupler.controller.middleware.exception;

public class XMLParseException extends RuntimeException {
  public XMLParseException(Throwable e) {
    super("An error occurred during parsing of XML content.", e);
  }
}
