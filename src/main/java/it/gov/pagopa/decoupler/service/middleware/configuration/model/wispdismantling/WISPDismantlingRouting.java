package it.gov.pagopa.decoupler.service.middleware.configuration.model.wispdismantling;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WISPDismantlingRouting {

  @JsonProperty("whitelist")
  private WISPDismantlingWhitelist whitelist;
}
