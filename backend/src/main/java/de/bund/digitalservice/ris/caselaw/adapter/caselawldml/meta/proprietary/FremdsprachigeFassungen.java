package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.FrbrLanguage;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class FremdsprachigeFassungen {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Fremdsprachige Fassungen";

  @SuppressWarnings("java:S1700")
  @XmlElement(name = "fremdsprachigeFassung", namespace = CaseLawLdml.RIS_NS)
  private List<FremdsprachigeFassung> fremdsprachigeFassungen;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class FremdsprachigeFassung {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Fremdsprachige Fassung";

    @XmlElement(name = "FRBRlanguage", namespace = CaseLawLdml.AKN_NS)
    private FrbrLanguage frbrLanguage;

    @XmlElement(name = "documentRef", namespace = CaseLawLdml.AKN_NS)
    private DocumentRef documentRef;
  }
}
