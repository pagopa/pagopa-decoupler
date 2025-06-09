package it.gov.pagopa.decoupler.client;

import it.gov.pagopa.decoupler.controller.middleware.exception.SOAPClientException;
import it.gov.pagopa.decoupler.controller.middleware.exception.XMLParseException;
import it.gov.pagopa.decoupler.service.middleware.mapper.XMLParser;
import it.gov.pagopa.decoupler.service.model.XMLContent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.mapstruct.ap.internal.util.Strings;

@ApplicationScoped
public class WISPDismantlingSOAPClient {

  @ConfigProperty(name = "decoupler.wisp-dismantling.base-url")
  private String baseURL;

  @RestClient private final SOAPClient soapClient;

  private final XMLParser xmlParser;

  public WISPDismantlingSOAPClient(SOAPClient soapClient, XMLParser parser) {
    this.soapClient = soapClient;
    this.xmlParser = parser;
  }

  public XMLContent send(XMLContent request) {

    XMLContent xmlResponse;
    try {
      RestClientBuilder clientBuilder = RestClientBuilder.newBuilder();
      clientBuilder.baseUri(new URI(this.baseURL));
      request.getHeaders().forEach(clientBuilder::header);
      SOAPClient client = clientBuilder.build(SOAPClient.class);

      try (Response response = client.send(request.asRawString())) {

        String rawXMLResponse = response.readEntity(String.class);
        xmlResponse = XMLContent.fromRaw(this.xmlParser, rawXMLResponse);
        for (Map.Entry<String, List<Object>> entry : response.getHeaders().entrySet()) {
          xmlResponse.addHeader(entry.getKey(), Strings.join(entry.getValue(), ","));
        }
        xmlResponse.build();
      }

    } catch (URISyntaxException e) {
      throw new SOAPClientException("Invalid base URL used for communication.", e);
    } catch (XMLParseException e) {
      throw new SOAPClientException("Received response not parseable as XML content.", e);
    }

    return xmlResponse;
  }
}
