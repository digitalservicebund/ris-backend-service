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
@XmlDiscriminatorValue("opinions")
public class Opinions extends AknBlock {

  @XmlElement(name = "opinion", namespace = CaseLawLdml.AKN_NS)
  private Opinion opinion;

  public static Opinions build(String content) {
    if (StringUtils.isBlank(content)) {
      return null;
    }
    return new Opinions(new Opinion(content));
  }

  public String getName() {
    return "opinions";
  }
}
