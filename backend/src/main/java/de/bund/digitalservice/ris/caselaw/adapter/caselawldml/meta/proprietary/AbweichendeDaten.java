package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlValue;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class AbweichendeDaten {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Abweichende Daten";

  @XmlElement(name = "abweichendesDatum", namespace = CaseLawLdml.RIS_NS)
  private List<AbweichendesDatum> daten;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class AbweichendesDatum {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Abweichendes Datum";

    @XmlValue private String value;
  }
}
