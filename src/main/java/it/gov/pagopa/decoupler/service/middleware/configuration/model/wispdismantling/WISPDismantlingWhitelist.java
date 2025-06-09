package it.gov.pagopa.decoupler.service.middleware.configuration.model.wispdismantling;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class WISPDismantlingWhitelist {

  @JsonProperty("creditor_institutions")
  private List<String> creditorInstitutions;

  @JsonProperty("stations")
  private List<String> stations;

  @JsonProperty("brokers")
  private List<String> brokers;

  @JsonProperty("channels")
  private List<String> channel;

  @JsonProperty("nodoinviarpt_paymenttypes")
  private List<String> paymentTypesForNodoInviaRPT;
}
