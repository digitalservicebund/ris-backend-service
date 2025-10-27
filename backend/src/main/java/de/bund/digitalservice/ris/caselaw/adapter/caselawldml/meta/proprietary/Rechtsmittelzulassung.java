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
  @Builder.Default
  @XmlAttribute(name = "domainTerm")
  private String domainTerm = "Rechtsmittelzulassung";

  @XmlElement(name = "rechtsmittelZugelassen", namespace = CaseLawLdml.RIS_NS)
  private RechtsmittelZugelassen rechtsmittelZugelassen;

  @XmlElement(name = "rechtsmittelZugelassenDurch", namespace = CaseLawLdml.RIS_NS)
  private RechtsmittelZugelassenDurch rechtsmittelZugelassenDurch;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  public static class RechtsmittelZugelassen {
    @XmlAttribute(name = "domainTerm")
    private String domainTerm = "Rechtsmittel zugelassen";

    @XmlValue private boolean value;

    public RechtsmittelZugelassen(boolean value) {
      this.value = value;
    }
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  public static class RechtsmittelZugelassenDurch {
    @XmlAttribute(name = "domainTerm")
    private String domainTerm = "Rechtsmittel zugelassen durch";

    @XmlValue private String value;

    public RechtsmittelZugelassenDurch(String value) {
      this.value = value;
    }
  }
}
