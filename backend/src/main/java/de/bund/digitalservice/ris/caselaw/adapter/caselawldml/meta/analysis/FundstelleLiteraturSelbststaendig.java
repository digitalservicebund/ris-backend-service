package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis;

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
public class FundstelleLiteraturSelbststaendig {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Fundstelle selbstständige Literatur";

  @XmlElement(name = "titel", namespace = CaseLawLdml.RIS_NS)
  private Titel titel;

  @XmlElement(name = "zitatstelle", namespace = CaseLawLdml.RIS_NS)
  private Zitatstelle zitatstelle;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Titel {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Titel";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Zitatstelle {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Zitatstelle";

    @XmlValue private String value;
  }
}
