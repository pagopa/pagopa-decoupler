package it.gov.pagopa.decoupler.controller;

import it.gov.pagopa.decoupler.controller.interfaces.IInfoController;
import it.gov.pagopa.decoupler.controller.model.common.InfoResponse;
import it.gov.pagopa.decoupler.service.HealthCheckService;

public class InfoController implements IInfoController {

  private final HealthCheckService healthCheckService;

  public InfoController(HealthCheckService healthCheckService) {
    this.healthCheckService = healthCheckService;
  }

  @Override
  public InfoResponse healthCheck() {
    return healthCheckService.getHealthInfo();
  }
}
