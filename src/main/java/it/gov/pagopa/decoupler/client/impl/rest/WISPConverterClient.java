package it.gov.pagopa.decoupler.client.impl.rest;

import it.gov.pagopa.decoupler.client.interfaces.rest.WISPDismantlingClient;
import it.gov.pagopa.decoupler.client.model.RPTTimerCreation;
import it.gov.pagopa.decoupler.controller.middleware.exception.SOAPClientException;
import it.gov.pagopa.decoupler.service.middleware.mapper.XMLParser;
import jakarta.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class WISPConverterClient {

  @ConfigProperty(name = "decoupler.wisp-dismantling.converter.base-url")
  private String baseURL;

  @RestClient private final WISPDismantlingClient client;

  private final XMLParser xmlParser;

  public WISPConverterClient(WISPDismantlingClient client, XMLParser parser) {
    this.client = client;
    this.xmlParser = parser;
  }

  public void sendRPTTimerCreation(String sessionId) {

    try {
      RestClientBuilder clientBuilder = RestClientBuilder.newBuilder();
      clientBuilder.baseUri(new URI(this.baseURL));
      WISPDismantlingClient client = clientBuilder.build(WISPDismantlingClient.class);

      // In asynchronous way, send the request in order to
      // execute RPT Timer creation
      RPTTimerCreation request = RPTTimerCreation.builder().sessionId(sessionId).build();
      client.sendRPTTimerCreation(request);

    } catch (URISyntaxException e) {
      throw new SOAPClientException("Invalid base URL used for communication.", e);
    }
  }
}
