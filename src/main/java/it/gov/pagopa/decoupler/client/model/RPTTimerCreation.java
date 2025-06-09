package it.gov.pagopa.decoupler.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RPTTimerCreation {

  @JsonProperty("sessionId")
  private String sessionId;
}
