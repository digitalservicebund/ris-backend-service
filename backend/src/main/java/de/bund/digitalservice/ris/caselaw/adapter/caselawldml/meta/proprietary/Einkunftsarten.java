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

    @XmlElement(name = "artDerEinkunft", namespace = CaseLawLdml.RIS_NS)
    private ArtDerEinkunft artDerEinkunft;

    @XmlElement(name = "begrifflichkeit", namespace = CaseLawLdml.RIS_NS)
    private Begrifflichkeit begrifflichkeit;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class ArtDerEinkunft {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Einkunftsart";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Begrifflichkeit {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Begrifflichkeit";

    @XmlValue private String value;
  }
}
