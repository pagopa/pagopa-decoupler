package it.gov.pagopa.decoupler.controller;

import it.gov.pagopa.decoupler.controller.model.InfoResponse;
import it.gov.pagopa.decoupler.service.HealthCheckService;

public class InfoController {

  private final HealthCheckService healthCheckService;

  public InfoController(HealthCheckService healthCheckService) {
    this.healthCheckService = healthCheckService;
  }

  public InfoResponse healthCheck() {
    return healthCheckService.getHealthInfo();
  }
}
