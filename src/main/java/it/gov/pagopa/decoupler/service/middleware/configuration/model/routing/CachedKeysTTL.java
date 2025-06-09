package it.gov.pagopa.decoupler.service.middleware.configuration.model.routing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CachedKeysTTL {

  @JsonProperty("fiscalcode_noticenumber")
  private long fiscalCodeNoticeNumberKeyTTL;

  @JsonProperty("fiscalcode_iuv")
  private long fiscalCodeIUVKeyTTL;

  @JsonProperty("paymenttoken")
  private long paymentTokenKeyTTL;

  @JsonProperty("ecommerce_transactionid")
  private long eCommerceTransactionIdKeyTTL;
}
