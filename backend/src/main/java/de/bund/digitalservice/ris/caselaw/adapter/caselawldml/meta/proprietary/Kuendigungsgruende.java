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
public class Kuendigungsgruende {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Kündigungsgründe";

  @SuppressWarnings("java:S1700")
  @XmlElement(name = "kuendigungsgrund", namespace = CaseLawLdml.RIS_NS)
  private List<Kuendigungsgrund> kuendigungsgruende;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Kuendigungsgrund {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Kündigungsgrund";

    @XmlValue private String value;
  }
}
