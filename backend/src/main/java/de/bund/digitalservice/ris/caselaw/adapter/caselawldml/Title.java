package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorValue;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@XmlDiscriminatorValue("title")
public class Title extends AknBlock {

  @XmlElement(name = "docTitle", namespace = CaseLawLdml.AKN_NS)
  private DocTitle docTitle;

  public static AknBlock build(String content) {
    if (StringUtils.isBlank(content)) {
      return null;
    }
    return new Title(new DocTitle(JaxbHtml.build(content)));
  }

  public String getName() {
    return "title";
  }
}
