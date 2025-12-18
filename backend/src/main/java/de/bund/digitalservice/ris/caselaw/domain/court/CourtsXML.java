package de.bund.digitalservice.ris.caselaw.domain.court;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import java.util.List;
import lombok.Data;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

@Data
@JsonRootName("juris-table")
public class CourtsXML {
  @JacksonXmlElementWrapper(useWrapping = false)
  @JsonProperty(value = "juris-gericht")
  List<CourtXML> list;
}
