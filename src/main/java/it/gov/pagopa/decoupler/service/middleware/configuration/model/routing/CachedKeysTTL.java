package it.gov.pagopa.decoupler.service.middleware.configuration.model.routing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CachedKeysTTL {

  @JsonProperty("fiscalcode_noticenumber")
  private int fiscalCodeNoticeNumberKeyTTL;

  @JsonProperty("fiscalcode_iuv")
  private int fiscalCodeIUVKeyTTL;

  @JsonProperty("paymenttoken")
  private int paymentTokenKeyTTL;

  @JsonProperty("ecommerce_transactionid")
  private int eCommerceTransactionIdKeyTTL;

  @JsonProperty("wisp_sessionid_validity_before_redirect")
  private int wispSessionIdValidityBeforeRedirect;
}
