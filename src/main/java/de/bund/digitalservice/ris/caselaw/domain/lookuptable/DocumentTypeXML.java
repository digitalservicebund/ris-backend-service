package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class DocumentTypeXML {
  @JacksonXmlProperty(isAttribute = true)
  long id;

  @JacksonXmlProperty(isAttribute = true, localName = "aenddatum_mail")
  String changeDateMail;

  @JacksonXmlProperty(isAttribute = true, localName = "aenddatum_client")
  String changeDateClient;

  @JacksonXmlProperty(isAttribute = true, localName = "aendkz")
  char changeIndicator; // Änderungskennzeichen

  @JacksonXmlProperty(isAttribute = true)
  String version;

  @JsonProperty(value = "jurisabk")
  String jurisShortcut;

  @JsonProperty(value = "dokumentart")
  char documentType;

  @JsonProperty(value = "mehrfach")
  String multiple;

  @JsonProperty(value = "bezeichnung")
  String label;

  @JsonProperty(value = "ueberbezeichnung1")
  String superlabel1;

  @JsonProperty(value = "ueberbezeichnung2")
  String superlabel2;
}
