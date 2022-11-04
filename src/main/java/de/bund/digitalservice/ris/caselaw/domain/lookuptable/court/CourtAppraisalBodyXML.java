package de.bund.digitalservice.ris.caselaw.domain.lookuptable.court;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CourtAppraisalBodyXML {

  @JsonProperty(value = "name")
  String name;

  @JsonProperty(value = "zahlangabe")
  CourtNumberSpecificationXML numberspecification;
}
