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
public class Vorgaenge {
  @Builder.Default
  @XmlAttribute(name = "domainTerm")
  private String domainTerm = "Vorgänge";

  @XmlElement(name = "vorgang", namespace = CaseLawLdml.RIS_NS)
  private List<Vorgang> vorgaenge;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Vorgang {
    @Builder.Default
    @XmlAttribute(name = "domainTerm")
    private String domainTerm = "Vorgang";

    @XmlValue private String value;
  }
}
