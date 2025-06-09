package it.gov.pagopa.decoupler.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PrimitiveInfo {

  private String primitive;

  @JsonProperty("type")
  private String type;

  @JsonProperty("external_subpath")
  private String externalSubpath;

  @JsonProperty("internal_subpath")
  private String internalSubpath;

  @JsonProperty("path_to_creditor_institution_field")
  private String pathToCreditorInstitutionField;

  @JsonProperty("path_to_station_field")
  private String pathToStationField;

  @JsonProperty("path_to_channel_field")
  private String pathToChannelField;

  @JsonProperty("path_to_brokerpsp_field")
  private String pathToBrokerPSPField;

  @JsonProperty("path_to_password_field")
  private String pathToPasswordField;

  @JsonProperty("insert_password_field_after_tag")
  private String pwdFieldAfterTag;

  @JsonProperty("password_field_position")
  private int pwdFieldPosition;

  @JsonProperty("deprecated")
  private boolean deprecated;

  @JsonProperty("dismissed")
  private boolean dismissed;
}
