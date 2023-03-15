package de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "juris-table")
public class FieldsOfLawXml {
  @JsonProperty(value = "juris-sachg")
  @JacksonXmlElementWrapper(useWrapping = false)
  List<FieldOfLawXml> list;
}
