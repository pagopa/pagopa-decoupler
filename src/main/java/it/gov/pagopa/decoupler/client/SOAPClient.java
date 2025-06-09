package it.gov.pagopa.decoupler.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public interface SOAPClient {

  @POST
  @Consumes(MediaType.TEXT_XML)
  @Produces(MediaType.TEXT_XML)
  Response send(String body);
}
