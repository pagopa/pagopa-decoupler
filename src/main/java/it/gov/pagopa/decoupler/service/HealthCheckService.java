package it.gov.pagopa.decoupler.service;

import it.gov.pagopa.decoupler.controller.model.common.InfoResponse;
import it.gov.pagopa.decoupler.util.logging.AppMessageUtil;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class HealthCheckService {

  @ConfigProperty(name = "app.name", defaultValue = "app")
  String name;

  @ConfigProperty(name = "app.version", defaultValue = "0.0.0")
  String version;

  @ConfigProperty(name = "app.environment", defaultValue = "local")
  String environment;

  public InfoResponse getHealthInfo() {

    // Generate Health check response
    return InfoResponse.builder()
        .name(name)
        .version(version)
        .environment(environment)
        .description(AppMessageUtil.getMessage("app.description"))
        .build();
  }
}
