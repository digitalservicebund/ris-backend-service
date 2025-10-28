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
  @Builder.Default
  @XmlAttribute(name = "domainTerm")
  private String domainTerm = "Sachgebiete";

  @SuppressWarnings("java:S1700")
  @XmlElement(name = "sachgebiet", namespace = CaseLawLdml.RIS_NS)
  private List<Sachgebiet> sachgebiete;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Sachgebiet {
    @Builder.Default
    @XmlAttribute(name = "domainTerm")
    private String domainTerm = "Sachgebiet";

    @XmlAttribute(name = "notation")
    private String notation;

    @XmlValue private String value;
  }
}
