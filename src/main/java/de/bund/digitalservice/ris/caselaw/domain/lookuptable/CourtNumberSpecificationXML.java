package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class CourtNumberSpecificationXML {

  @JacksonXmlProperty(isAttribute = true, localName = "typ")
  char type;

  @JsonProperty(value = "vonnr")
  String fromnumber;

  @JsonProperty(value = "bisnr")
  String tonumber;
}
