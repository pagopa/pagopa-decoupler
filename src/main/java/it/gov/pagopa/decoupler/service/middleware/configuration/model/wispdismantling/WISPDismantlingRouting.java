package it.gov.pagopa.decoupler.service.middleware.configuration.model.wispdismantling;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WISPDismantlingRouting {

  @JsonProperty("is_enabled")
  private boolean enabled;

  @JsonProperty("whitelist")
  private WISPDismantlingWhitelist whitelist;
}
