package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CourtSynonymXML {

  @JsonProperty(value = "syntyp")
  String synonymType;

  @JsonProperty(value = "synbez")
  String synonymLabel;
}
