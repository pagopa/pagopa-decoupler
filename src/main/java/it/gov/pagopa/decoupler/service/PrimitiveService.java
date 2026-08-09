package it.gov.pagopa.decoupler.service;

import it.gov.pagopa.decoupler.client.impl.rest.WISPConverterClient;
import it.gov.pagopa.decoupler.client.impl.soap.NDPSOAPClient;
import it.gov.pagopa.decoupler.client.impl.soap.WISPSoapConverterClient;
import it.gov.pagopa.decoupler.service.middleware.cache.RedisCacheService;
import it.gov.pagopa.decoupler.service.middleware.configuration.DecouplerConfiguration;
import it.gov.pagopa.decoupler.service.middleware.configuration.PrimitiveInfoRegistry;
import it.gov.pagopa.decoupler.service.middleware.configuration.model.wispdismantling.WISPDismantlingRouting;
import it.gov.pagopa.decoupler.service.middleware.configuration.model.wispdismantling.WISPDismantlingWhitelist;
import it.gov.pagopa.decoupler.service.middleware.mapper.XMLParser;
import it.gov.pagopa.decoupler.service.model.PrimitiveInfo;
import it.gov.pagopa.decoupler.service.model.RequestProp;
import it.gov.pagopa.decoupler.service.model.XMLContent;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

  private final RedisCacheService cacheService;

  private final WISPSoapConverterClient wispSoapConverterClient;

  private final WISPConverterClient wispConverterClient;

  private final NDPSOAPClient ndpSOAPClient;

  private final DecouplerConfiguration decouplerConfig;

  private final PrimitiveInfoRegistry primitiveRegistry;

  private final XMLParser xmlParser;

  public PrimitiveService(
      RedisCacheService cacheService,
      WISPSoapConverterClient wispSoapConverterClient,
      WISPConverterClient wispConverterClient,
      NDPSOAPClient ndpSOAPClient,
      PrimitiveInfoRegistry primitiveRegistry,
      DecouplerConfiguration decouplerConfig,
      XMLParser xmlParser) {

    this.cacheService = cacheService;
    this.wispSoapConverterClient = wispSoapConverterClient;
    this.wispConverterClient = wispConverterClient;
    this.ndpSOAPClient = ndpSOAPClient;
    this.primitiveRegistry = primitiveRegistry;
    this.decouplerConfig = decouplerConfig;
    this.xmlParser = xmlParser;
  }

  public XMLContent handleNodoInviaRPT(XMLContent request) {

    XMLContent response;

    // ===== INBOUND (pre-request) =====
    // policy: ndphost-header
    setNDPHostHeaderForPerformanceEnv(request);

    // policy: ndp-nuova-connettivita
    setPasswordForNuovaConnettivita(request);

    // policy: ndp-wisp-nodoinviarpt-nodoinviacarrellorpt-inbound-policy
    boolean isRequestForWispDismantling = mustBeRoutedToWISPDismantling(request);
    if (isRequestForWispDismantling) {

      // backend
      response = this.wispSoapConverterClient.send(request);

      // policy: ndp-wisp-nodoinviarpt-nodoinviacarrellorpt-outbound-policy
      taggingResponseForWISPDismantling(response, request);
    }

    //
    else {

      // policy: ndp-rpt-inbound-policy
      ndpRPTInboundPolicy(request);

      // policy: ndp-set-base-url-policy
      String baseURL = ndpSetBaseURL(request);

      response = this.ndpSOAPClient.send(baseURL, request);
    }

    // ===== OUTBOUND (post-request) =====

    return response;
  }

  private void setNDPHostHeaderForPerformanceEnv(XMLContent request) {

    // TODO this must be set as header in APIM
    // ((string)context.Request.OriginalUrl.ToUri().Host).Equals("api.prf.platform.pagopa.it"))
    String originalHost = request.getHeader("x-orginal-host-for");
    if (this.perfEnvironmentDNS.equals(originalHost)) {
      request.addHeader("ndphost", this.perfEnvironmentNdphostHeader);
    }
  }

  private void setPasswordForNuovaConnettivita(XMLContent request) {

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
        primitiveInfo.getPathToPasswordField(),
        nuovaConnettivitaPassword,
        primitiveInfo.getPwdFieldPosition());
    request.addHeader("x-forwarded-for", xForwardedForValue);
  }

  /**
   * TODO: add more description for this Javadoc The result is true if: - channelId, brokerPSP,
   * stationId and creditorInstitutionId belong to relative lists (or contains '*' character) - the
   * 'tipoVersamento' field is accepted for nodoInviaRPT
   *
   * @param request
   * @return
   */
  private boolean mustBeRoutedToWISPDismantling(XMLContent request) {

    // End prematurely if WISP Dismantling handling is not enabled
    WISPDismantlingRouting wispDismantlingRouting =
        this.decouplerConfig.getNDPRouting().getWispDismantling();
    if (!wispDismantlingRouting.isEnabled()) {
      return false;
    }

    // First, extract all the whitelists from configuration
    WISPDismantlingWhitelist wispDismantlingWhitelist = wispDismantlingRouting.getWhitelist();
    List<String> brokersInWhitelist = wispDismantlingWhitelist.getBrokers();
    List<String> channelsInWhitelist = wispDismantlingWhitelist.getChannel();
    List<String> stationsInWhitelist = wispDismantlingWhitelist.getStations();
    List<String> creditorInstitutionsInWhitelist =
        wispDismantlingWhitelist.getCreditorInstitutions();

    // Extract primitive name and retrieve all required fields
    String primitive = request.getProp(RequestProp.PRIMITIVE_NAME, String.class);
    PrimitiveInfo primitiveInfo = this.primitiveRegistry.fromPrimitive(primitive);
    String creditorInstitution =
        request.getFieldAsString(primitiveInfo.getPathToCreditorInstitutionField(), "-");

    // Check if all entities are whitelisted due to ALL clause (i.e. the star character '*')
    boolean areAllBrokersWhitelisted = brokersInWhitelist.contains("*");
    boolean areAllChannelsWhitelisted = brokersInWhitelist.contains("*");
    boolean areAllStationsWhitelisted = brokersInWhitelist.contains("*");
    boolean areAllCreditorInstitutionsWhitelisted = brokersInWhitelist.contains("*");
    if (areAllBrokersWhitelisted
        && areAllChannelsWhitelisted
        && areAllStationsWhitelisted
        && areAllCreditorInstitutionsWhitelisted) {
      return true;
    }

    // Check if broker PSP and channel are whitelisted for WISP Dismantling routing, and end
    // prematurely if one of them is false.
    String channel = request.getFieldAsString(primitiveInfo.getPathToChannelField(), "-");
    String brokerPSP = request.getFieldAsString(primitiveInfo.getPathToBrokerPSPField(), "-");
    boolean isBrokerWhitelisted =
        areAllBrokersWhitelisted || brokersInWhitelist.contains(brokerPSP);
    boolean isChannelWhitelisted =
        areAllChannelsWhitelisted || channelsInWhitelist.contains(channel);
    if (!isBrokerWhitelisted || !isChannelWhitelisted) {
      return false;
    }

    // Check if creditor institution is whitelisted for WISP Dismantling routing.
    // In order to do so, the relation between creditor institution and station is evaluated and
    // will be defined as "whitelisted" if the record, written in "CI-station" format, is defined
    // (or, if all creditor institution/station are whitelisted with '*').
    boolean areCreditorInstitutionAndStationWhitelisted;
    if (!areAllCreditorInstitutionsWhitelisted
        && creditorInstitutionsInWhitelist.contains(creditorInstitution)) {

      // Retrieve only the creditor institution-station relation that starts with the request
      // domainId
      Set<String> stationsInWhitelistForCreditorInstitution =
          stationsInWhitelist.stream()
              .filter(station -> station.startsWith(creditorInstitution + "-"))
              .collect(Collectors.toSet());

      // If something is extracted, check if creditor institution/station relation is included in
      // whitelist.
      // Otherwise, the relation is automatically not in whitelist.
      if (!stationsInWhitelistForCreditorInstitution.isEmpty()) {
        String stationId = request.getFieldAsString(primitiveInfo.getPathToStationField(), "");
        areCreditorInstitutionAndStationWhitelisted =
            stationsInWhitelistForCreditorInstitution.contains(
                creditorInstitution + "-" + stationId);
      } else {
        areCreditorInstitutionAndStationWhitelisted = false;
      }

    } else {
      areCreditorInstitutionAndStationWhitelisted = areAllCreditorInstitutionsWhitelisted;
    }

    // Check if 'tipoVersamento' field is accepted for WISP dismantling. This check is made only
    // if the primitive name is 'nodoInviaRPT'
    boolean hasAllowedPaymentType = true;
    if ("nodoInviaRPT".equalsIgnoreCase(primitive)) {

      try {
        // Extract RPT content and decode it from Base64
        String base64RPT =
            request.getFieldAsString(
                "Envelope.Body.nodoInviaCarrelloRPT.listaRPT.elementoListaRPT[0].rpt", "");
        String rptRawString = new String(Base64.getDecoder().decode(base64RPT));

        // Extract the XML content as a map and then search the 'tipoVersamento' field
        XMLContent rtp = XMLContent.fromRaw(this.xmlParser, rptRawString).build();
        String paymentType = rtp.getFieldAsString("RPT.datiVersamento[0].tipoVersamento", "-");

        // Then, check if the type is one of the allowed ones
        hasAllowedPaymentType =
            wispDismantlingWhitelist.getPaymentTypesForNodoInviaRPT().contains(paymentType);

      } catch (IllegalArgumentException e) {

        // TODO: don't block execution, as the validation process for RPT content is not done in
        // Decoupler. Maybe log something in error.
        hasAllowedPaymentType = false;
      }
    }

    // Finally, return all conditions in AND for the final result
    return areCreditorInstitutionAndStationWhitelisted && hasAllowedPaymentType;
  }

  private void ndpRPTInboundPolicy(XMLContent request) {}

  private String ndpSetBaseURL(XMLContent request) {
    return "";
  }

  private void taggingResponseForWISPDismantling(XMLContent response, XMLContent request) {

    String contentFieldSubpath =
        String.format(
            "Envelope.Body.%sRisposta", request.getProp(RequestProp.PRIMITIVE_NAME, String.class));
    Object fault = response.getField(contentFieldSubpath + ".fault");
    if (fault != null) {
      return;
    }

    String redirectUrl = response.getFieldAsString(contentFieldSubpath + ".url", "-");
    String[] splitRedirectUrl = redirectUrl.split("idSession=");
    if (splitRedirectUrl.length <= 1) {
      return;
    }

    String sessionId = splitRedirectUrl[1];
    String wispSessionIdKey = "wisp_timer_hang_" + sessionId;
    int ttl =
        this.decouplerConfig
            .getNDPRouting()
            .getCachedKeysTTL()
            .getWispSessionIdValidityBeforeRedirect();
    this.cacheService.store(wispSessionIdKey, LocalDateTime.now().plusSeconds(ttl).toString(), ttl);
    this.wispConverterClient.sendRPTTimerCreation(sessionId);
  }
}
