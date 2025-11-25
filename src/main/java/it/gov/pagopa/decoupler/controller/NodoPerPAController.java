package it.gov.pagopa.decoupler.controller;

import it.gov.pagopa.decoupler.controller.interfaces.INodoPerPAController;
import it.gov.pagopa.decoupler.controller.middleware.exception.XMLParseException;
import it.gov.pagopa.decoupler.service.PrimitiveService;
import it.gov.pagopa.decoupler.service.middleware.mapper.XMLParser;
import it.gov.pagopa.decoupler.service.model.RequestProp;
import it.gov.pagopa.decoupler.service.model.XMLContent;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

public class NodoPerPAController implements INodoPerPAController {

  private XMLParser parser;

  private PrimitiveService primitiveService;

  public NodoPerPAController(XMLParser parser, PrimitiveService primitiveService) {

    this.parser = parser;
    this.primitiveService = primitiveService;
  }

  @Override
  public Response handleNodoInviaRPTinAuth(String body, @Context HttpHeaders headers)
      throws XMLParseException {

    XMLContent request =
        XMLContent.fromRaw(this.parser, body)
            .withHeaders(headers)
            .withProp(RequestProp.IS_ON_NUOVA_CONNETTIVITA, true)
            .build();
    XMLContent response = primitiveService.handleNodoInviaRPT(request);
    Response.ResponseBuilder responseBuilder = Response.status(200).entity(response.asRawString());
    response.getHeaders().forEach((key, value) -> responseBuilder.header(key, value));
    return responseBuilder.build();
  }
}
