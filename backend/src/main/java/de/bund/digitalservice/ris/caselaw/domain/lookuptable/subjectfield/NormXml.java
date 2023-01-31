package de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NormXml {
  @JsonProperty(value = "normabk")
  String shortcut;

  @JsonProperty(value = "enbez")
  String enbez;
}
