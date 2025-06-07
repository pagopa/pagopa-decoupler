package it.gov.pagopa.decoupler.service;

import it.gov.pagopa.decoupler.service.middleware.configuration.PrimitiveInfoRegistry;
import it.gov.pagopa.decoupler.service.model.PrimitiveInfo;
import it.gov.pagopa.decoupler.service.model.RequestProp;
import it.gov.pagopa.decoupler.service.model.XMLContent;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class PrimitiveService {

  @ConfigProperty(name = "decoupler.prf.dns")
  private String perfEnvironmentDNS;

  @ConfigProperty(name = "decoupler.prf.ndphost-header")
  private String perfEnvironmentNdphostHeader;

  @ConfigProperty(name = "decoupler.nuova-connettivita.password")
  private String nuovaConnettivitaPassword;

  @ConfigProperty(name = "decoupler.nuova-connettivita.x-forwarded-for")
  private String xForwardedForValue;

  private final PrimitiveInfoRegistry primitiveRegistry;

  public PrimitiveService(PrimitiveInfoRegistry primitiveRegistry) {

    this.primitiveRegistry = primitiveRegistry;
  }

  public String handleNodoInviaRPT(XMLContent request) {

    // ===== INBOUND (pre-request) =====
    // policy: ndphost-header
    executeNdphostHeaderPolicy(request);

    // policy: ndp-nuova-connettivita
    executeNdpNuovaConnettivita(request);

    // TODO set baseUrl-baseNodeId and TTLs

    // policy: wisp-batch-migration
    executeWispBatchMigration(request);

    // policy: ndp-wisp-nodoinviarpt-nodoinviacarrellorpt-inbound-policy
    // if is_whitelisted=true -> send request to D-WISP, otherwise continue
    // policy: ndp-rpt-inbound-policy
    // policy: ndp-set-base-url-policy
    // ===== EXECUTE REQUEST =====
    // ===== OUTBOUND (post-request) =====
    // policy: ndp-wisp-nodoinviarpt-nodoinviacarrellorpt-outbound-policy

    return null;
  }

  private void executeNdphostHeaderPolicy(XMLContent request) {

    // TODO this must be set as header in APIM
    // ((string)context.Request.OriginalUrl.ToUri().Host).Equals("api.prf.platform.pagopa.it"))
    String originalHost = request.getHeader("x-orginal-host-for");
    if (this.perfEnvironmentDNS.equals(originalHost)) {
      request.addHeader("ndphost", this.perfEnvironmentNdphostHeader);
    }
  }

  private void executeNdpNuovaConnettivita(XMLContent request) {

    // If the request is not made on Nuova Connettività, skip this elaboration
    if (!request.getProp(RequestProp.IS_ON_NUOVA_CONNETTIVITA, Boolean.class)) {
      return;
    }

    // Extract primitive name and retrieve all static properties
    String primitive = request.getProp(RequestProp.PRIMITIVE_NAME, String.class);
    PrimitiveInfo primitiveInfo = this.primitiveRegistry.fromPrimitive(primitive);

    // If password field already exists, simply replace it with new password.
    // If password field does not exist, include it at the required position.
    // Finally, include changes on request and add a new header.
    request.setField(
        primitiveInfo.getPathToPwdField(),
        nuovaConnettivitaPassword,
        primitiveInfo.getPwdFieldPosition());
    request.addHeader("x-forwarded-for", xForwardedForValue);
  }

  private void executeWispBatchMigration(XMLContent request) {}
}
