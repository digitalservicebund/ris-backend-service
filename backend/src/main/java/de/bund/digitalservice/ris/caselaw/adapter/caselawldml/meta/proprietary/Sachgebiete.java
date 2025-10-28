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
public class Sachgebiete {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Sachgebiete";

  @SuppressWarnings("java:S1700")
  @XmlElement(name = "sachgebiet", namespace = CaseLawLdml.RIS_NS)
  private List<Sachgebiet> sachgebiete;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Sachgebiet {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Sachgebiet";

    @XmlAttribute(name = "notation")
    private String notation;

    @XmlValue private String value;
  }
}
