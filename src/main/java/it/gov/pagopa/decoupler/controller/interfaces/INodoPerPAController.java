package it.gov.pagopa.decoupler.controller.interfaces;

import it.gov.pagopa.decoupler.controller.model.error.ErrorResponse;
import it.gov.pagopa.decoupler.util.constant.SOAPControllerConstants;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/nodo-per-pa")
@Tag(name = "Nodo-per-PA Primitives", description = "Nodo-per-PA SOAP primitive operations")
public interface INodoPerPAController {

  @POST
  @Path("/nodoInviaRPT")
  @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
  @Consumes({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
  @Operation(
      operationId = "ISoapPrimitiveController_handleNodoInviaRPT",
      summary = "Decoupler for [PRIMITIVE] primitive",
      description =
          "Execute the decoupling logic on [PRIMITIVE] primitive. [TODO: insert more description"
              + " about logic]")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200 [OK]",
            description = "Success",
            content =
                @Content(
                    mediaType = MediaType.TEXT_XML,
                    schema = @Schema(implementation = String.class),
                    example = SOAPControllerConstants.OPENAPI_XML_NODOINVIARPT_OK_EXAMPLE)),
        @APIResponse(
            responseCode = "200 [ERROR]",
            description = "Error",
            content =
                @Content(
                    mediaType = MediaType.TEXT_XML,
                    schema = @Schema(implementation = ErrorResponse.class),
                    example = SOAPControllerConstants.OPENAPI_XML_NODOINVIARPT_KO_EXAMPLE))
      })
  String handleNodoInviaRPTinAuth(String body, @Context HttpHeaders headers);
}
