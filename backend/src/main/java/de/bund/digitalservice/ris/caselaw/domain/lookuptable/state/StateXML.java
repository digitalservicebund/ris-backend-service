package de.bund.digitalservice.ris.caselaw.domain.lookuptable.state;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class StateXML {
  @JacksonXmlProperty(isAttribute = true)
  long id;

  @JacksonXmlProperty(isAttribute = true, localName = "aendkz")
  char changeIndicator;

  @JacksonXmlProperty(isAttribute = true)
  String version;

  @JsonProperty(value = "jurisabk")
  String jurisShortcut;

  @JsonProperty(value = "bezeichnung")
  String label;
}
