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
public class Definitionen {
  @XmlAttribute(namespace = CaseLawLdml.RIS_NS, name = "domainTerm")
  private static final String DOMAIN_TERM = "Definitionen";

  @SuppressWarnings("java:S1700")
  @XmlElement(name = "definition", namespace = CaseLawLdml.RIS_NS)
  private List<Definition> definitionen;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Definition {
    @XmlAttribute(namespace = CaseLawLdml.RIS_NS, name = "domainTerm")
    private static final String DOMAIN_TERM = "Definition";

    @XmlElement(name = "definierterBegriff", namespace = CaseLawLdml.RIS_NS)
    private DefinierterBegriff definierterBegriff;

    @XmlElement(name = "definierendeRandnummer", namespace = CaseLawLdml.RIS_NS)
    private DefinierendeRandnummer definierendeRandnummer;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder
    public static class DefinierterBegriff {
      @XmlAttribute(name = "domainTerm")
      private static final String DOMAIN_TERM = "Definierter Begriff";

      @XmlValue private String value;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder
    public static class DefinierendeRandnummer {
      @XmlAttribute(name = "domainTerm")
      private static final String DOMAIN_TERM = "Definierende Randnummer";

      @XmlAttribute(namespace = CaseLawLdml.AKN_NS, name = "refersTo")
      private String refersTo;

      @XmlValue private String value;
    }
  }
}
