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
public class Einkunftsarten {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Einkunftsarten";

  @XmlElement(name = "einkunftsart", namespace = CaseLawLdml.RIS_NS)
  private List<Einkunftsart> values;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Einkunftsart {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Einkunftsart";

    @XmlElement(name = "einkunftsartTyp", namespace = CaseLawLdml.RIS_NS)
    private EinkunftsartTyp einkunftsartTyp;

    @XmlElement(name = "einkunftsartBegrifflichkeit", namespace = CaseLawLdml.RIS_NS)
    private EinkunftsartBegrifflichkeit einkunftsartBegrifflichkeit;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class EinkunftsartTyp {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Einkunftsart";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class EinkunftsartBegrifflichkeit {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Begrifflichkeit";

    @XmlValue private String value;
  }
}
