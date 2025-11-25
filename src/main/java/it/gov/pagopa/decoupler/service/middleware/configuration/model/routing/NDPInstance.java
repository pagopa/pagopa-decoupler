package it.gov.pagopa.decoupler.service.middleware.configuration.model.routing;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class NDPInstance {

  @JsonProperty("node_id")
  private String nodeId;

  @JsonProperty("description")
  private String description;

  @JsonProperty("node_uri")
  private String nodeUri;

  @JsonProperty("routing")
  private Integer routing;

  @JsonProperty("cis")
  private List<String> creditorInstitutions;

  @JsonProperty("list_priority")
  private Integer listPriority;
}
