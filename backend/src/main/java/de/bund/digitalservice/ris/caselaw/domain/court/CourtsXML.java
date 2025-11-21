package de.bund.digitalservice.ris.caselaw.domain.court;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@Data
@JacksonXmlRootElement(localName = "juris-table")
public class CourtsXML {
  @JacksonXmlElementWrapper(useWrapping = false)
  @JsonProperty(value = "juris-gericht")
  List<CourtXML> list;
}
