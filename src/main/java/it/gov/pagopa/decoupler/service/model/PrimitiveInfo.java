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

  @JsonProperty("path_to_password_field")
  private String pathToPwdField;

  @JsonProperty("insert_password_field_after_tag")
  private String pwdFieldAfterTag;

  @JsonProperty("password_field_position")
  private int pwdFieldPosition;

  @JsonProperty("deprecated")
  private boolean deprecated;

  @JsonProperty("dismissed")
  private boolean dismissed;
}
