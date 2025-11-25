package it.gov.pagopa.decoupler.client.interfaces.rest;

import io.smallrye.mutiny.Uni;
import it.gov.pagopa.decoupler.client.model.RPTTimerCreation;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public interface WISPDismantlingClient {

  @POST
  @Path("/rpt/timer")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  Uni<Response> sendRPTTimerCreation(RPTTimerCreation body);
}
