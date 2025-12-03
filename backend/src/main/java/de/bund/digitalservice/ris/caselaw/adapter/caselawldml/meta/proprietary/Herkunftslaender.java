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
public class Herkunftslaender {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Herkunftsländer";

  @SuppressWarnings("java:S1700")
  @XmlElement(name = "herkunftsland", namespace = CaseLawLdml.RIS_NS)
  private List<Herkunftsland> herkunftslaender;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Herkunftsland {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Herkunftsland";

    @XmlElement(name = "herkunftslandAltwert", namespace = CaseLawLdml.RIS_NS)
    private HerkunftslandAltwert herkunftslandAltwert;

    @XmlElement(name = "herkunftslandLandbezeichnung", namespace = CaseLawLdml.RIS_NS)
    private Landbezeichnung landbezeichnung;

    @XmlElement(name = "herkunftslandRechtlicherRahmen", namespace = CaseLawLdml.RIS_NS)
    private HerkunftslandRechtlicherRahmen herkunftslandRechtlicherRahmen;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class HerkunftslandAltwert {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Altwert";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Landbezeichnung {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Landbezeichnung";

    @XmlAttribute(name = "notation")
    private String notation;

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class HerkunftslandRechtlicherRahmen {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Rechtlicher Rahmen";

    @XmlAttribute(name = "notation")
    private String notation;

    @XmlValue private String value;
  }
}
