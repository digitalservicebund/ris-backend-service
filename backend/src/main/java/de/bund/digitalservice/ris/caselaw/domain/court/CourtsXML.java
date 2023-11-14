package de.bund.digitalservice.ris.caselaw.domain.court;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "juris-table")
public class CourtsXML {
  @JacksonXmlElementWrapper(useWrapping = false)
  @JsonProperty(value = "juris-gericht")
  List<CourtXML> list;
}
