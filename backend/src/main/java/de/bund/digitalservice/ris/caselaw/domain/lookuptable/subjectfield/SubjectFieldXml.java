package de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.util.Set;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class SubjectFieldXml {
  @JsonProperty(value = "id")
  long id;

  @JsonProperty(value = "aenddatum_mail")
  String changeDateMail;

  @JsonProperty(value = "aenddatum_client")
  String changeDateClient;

  @JsonProperty(value = "aendkz")
  char changeIndicator;

  @JsonProperty String version;

  @JsonProperty(value = "sachgebiet")
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

  public String getSubjectFieldNumber() {
    if (subjectFieldNumber.endsWith("-")) {
      return StringUtils.chop(subjectFieldNumber);
    } else {
      return subjectFieldNumber;
    }
  }
}
