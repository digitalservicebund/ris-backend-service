package de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.Set;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class SubjectFieldXml {
  @JacksonXmlProperty(isAttribute = true, localName = "id")
  long id;

  @JacksonXmlProperty(isAttribute = true, localName = "aenddatum_mail")
  String changeDateMail;

  @JacksonXmlProperty(isAttribute = true, localName = "aenddatum_client")
  String changeDateClient;

  @JacksonXmlProperty(isAttribute = true, localName = "aendkz")
  char changeIndicator;

  @JacksonXmlProperty(isAttribute = true)
  String version;

  @JsonProperty(value = "sachgebiet") // TODO  @JacksonXmlProperty()
  String subjectFieldNumber;

  @JsonProperty(value = "stext")
  String subjectFieldText;

  @JsonProperty(value = "navbez")
  String navigationTerm;

  @JsonProperty(value = "schlagwort")
  @JacksonXmlElementWrapper(useWrapping = false)
  Set<String> keywords;

  @JsonProperty(value = "norm")
  @JacksonXmlElementWrapper(useWrapping = false)
  Set<NormXml> norms;

  public String getSubjectFieldNumber() { // TODO alte Datei hatte sachgebietsnummern wie '01-'
    if (subjectFieldNumber.endsWith("-")) {
      return StringUtils.chop(subjectFieldNumber);
    } else {
      return subjectFieldNumber;
    }
  }
}
