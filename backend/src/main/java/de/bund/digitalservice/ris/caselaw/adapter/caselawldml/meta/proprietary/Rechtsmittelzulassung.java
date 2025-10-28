package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Rechtsmittelzulassung {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Rechtsmittelzulassung";

  @XmlElement(name = "rechtsmittelZugelassen", namespace = CaseLawLdml.RIS_NS)
  private RechtsmittelZugelassen rechtsmittelZugelassen;

  @XmlElement(name = "rechtsmittelZugelassenDurch", namespace = CaseLawLdml.RIS_NS)
  private RechtsmittelZugelassenDurch rechtsmittelZugelassenDurch;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class RechtsmittelZugelassen {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Rechtsmittel zugelassen";

    @XmlValue private boolean value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class RechtsmittelZugelassenDurch {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Rechtsmittel zugelassen durch";

    @XmlValue private String value;
  }
}
