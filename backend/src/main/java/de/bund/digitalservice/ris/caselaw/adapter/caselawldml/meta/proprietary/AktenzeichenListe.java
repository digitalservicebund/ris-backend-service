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
@Builder(toBuilder = true)
public class AktenzeichenListe {
  @Builder.Default
  @XmlAttribute(name = "domainTerm")
  private String domainTerm = "Aktenzeichenliste";

  @XmlElement(name = "aktenzeichen", namespace = CaseLawLdml.RIS_NS)
  private List<Aktenzeichen> aktenzeichen;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Aktenzeichen {
    @Builder.Default
    @XmlAttribute(name = "domainTerm")
    private String domainTerm = "Aktenzeichen";

    @XmlAttribute(namespace = CaseLawLdml.AKN_NS, name = "refersTo")
    private String refersTo;

    @XmlValue private String value;
  }
}
