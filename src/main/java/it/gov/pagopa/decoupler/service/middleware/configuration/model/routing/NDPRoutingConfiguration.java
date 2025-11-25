package it.gov.pagopa.decoupler.service.middleware.configuration.model.routing;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.decoupler.service.middleware.configuration.model.wispdismantling.WISPDismantlingRouting;
import java.util.List;
import lombok.Data;

@Data
public class NDPRoutingConfiguration {

  @JsonProperty("default_node_id")
  private String defaultNodeId;

  @JsonProperty("cached_keys_ttl")
  private CachedKeysTTL cachedKeysTTL;

  @JsonProperty("wisp_dismantling")
  private WISPDismantlingRouting wispDismantling;

  @JsonProperty("instances")
  private List<NDPInstance> instances;
}
