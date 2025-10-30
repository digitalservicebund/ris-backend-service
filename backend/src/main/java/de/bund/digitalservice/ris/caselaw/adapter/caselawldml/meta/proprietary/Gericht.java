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
public class Gericht {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Gericht";

  @XmlAttribute(namespace = CaseLawLdml.AKN_NS, name = "refersTo")
  private String refersTo;

  @XmlElement(name = "typ", namespace = CaseLawLdml.RIS_NS)
  private GerichtTyp typ;

  @XmlElement(name = "ort", namespace = CaseLawLdml.RIS_NS)
  private GerichtOrt ort;

  @XmlElement(name = "spruchkoerper", namespace = CaseLawLdml.RIS_NS)
  private Spruchkoerper spruchkoerper;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class GerichtTyp {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Gerichtstyp";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class GerichtOrt {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Gerichtsort";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Spruchkoerper {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Spruchkörper";

    @XmlAttribute(namespace = CaseLawLdml.AKN_NS, name = "refersTo")
    private static final String REFERS_TO = "#spruchkoerper";

    @XmlValue private String value;
  }
}
