package it.gov.pagopa.decoupler.service.middleware.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.decoupler.service.middleware.configuration.model.routing.NDPRoutingConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;

@ApplicationScoped
public class DecouplerConfiguration {

  public NDPRoutingConfiguration ndpRouting;

  /**
   * TODO: currently mocked with values read from static file. This must be defined in a dynamic way
   * in order to handle activations, feature flags and disaster recovery related issues! Maybe these
   * values could be defined in CosmosDB or Nodo's CFG table.
   *
   * @return
   */
  public NDPRoutingConfiguration getNDPRouting() {

    if (this.ndpRouting == null) {
      try (InputStream inputStream =
          Thread.currentThread()
              .getContextClassLoader()
              .getResourceAsStream("config/ndp-routing.json")) {
        if (inputStream == null) {
          throw new RuntimeException("Cannot load NDP routing info. Missing configuration file.");
        }
        this.ndpRouting = new ObjectMapper().readValue(inputStream, new TypeReference<>() {});
      } catch (IOException e) {
        throw new RuntimeException("Failed to load NDP routing info from configuration file.", e);
      }
    }
    return this.ndpRouting;
  }
}
