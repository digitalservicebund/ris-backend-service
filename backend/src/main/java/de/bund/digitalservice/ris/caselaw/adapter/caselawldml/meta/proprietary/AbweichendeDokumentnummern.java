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
public class AbweichendeDokumentnummern {
  @Builder.Default
  @XmlAttribute(name = "domainTerm")
  private String domainTerm = "Abweichende Dokumentnummern";

  @XmlElement(name = "abweichendeDokumentnummer", namespace = CaseLawLdml.RIS_NS)
  private List<AbweichendeDokumentnummer> dokumentnummern;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class AbweichendeDokumentnummer {
    @Builder.Default
    @XmlAttribute(name = "domainTerm")
    private String domainTerm = "Abweichende Dokumentnummer";

    @XmlValue private String value;
  }
}
