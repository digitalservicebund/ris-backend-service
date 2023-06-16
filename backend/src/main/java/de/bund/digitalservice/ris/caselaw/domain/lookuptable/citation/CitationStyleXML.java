package de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class CitationStyleXML {
  @JacksonXmlProperty(isAttribute = true)
  long id;

  @JacksonXmlProperty(isAttribute = true, localName = "aendkz")
  char changeIndicator;

  @JacksonXmlProperty(isAttribute = true, localName = "aenddatum_mail")
  String changeDateMail;

  @JacksonXmlProperty(isAttribute = true)
  String version;

  @JsonProperty(value = "dok_dokumentart")
  String documentType;

  @JsonProperty(value = "zit_dokumentart")
  String citationDocumentType;

  @JsonProperty(value = "abk")
  String jurisShortcut;

  @JsonProperty(value = "bezeichnung")
  String label;
}
