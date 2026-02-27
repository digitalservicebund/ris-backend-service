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
public class DatenDerMuendlichenVerhandlung {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Daten der mündlichen Verhandlung";

  @XmlElement(name = "datumDerMuendlichenVerhandlung", namespace = CaseLawLdml.RIS_NS)
  private List<DatumDerMuendlichenVerhandlung> daten;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class DatumDerMuendlichenVerhandlung {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Datum der mündlichen Verhandlung";

    @XmlValue private String value;
  }
}
