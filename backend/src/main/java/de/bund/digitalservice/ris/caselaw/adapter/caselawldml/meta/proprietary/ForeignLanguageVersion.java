package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.FrbrLanguage;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ForeignLanguageVersion {

  @XmlElement(name = "FRBRlanguage", namespace = CaseLawLdml.AKN_NS)
  private FrbrLanguage frbrLanguage;

  @XmlElement(name = "documentRef", namespace = CaseLawLdml.AKN_NS)
  private DocumentRef documentRef;
}
