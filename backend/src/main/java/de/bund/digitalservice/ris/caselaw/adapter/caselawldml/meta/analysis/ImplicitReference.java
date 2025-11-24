package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ImplicitReference {
  @XmlAttribute(name = "domainTerm", namespace = CaseLawLdml.RIS_NS)
  @NonNull
  private String domainTerm;

  @XmlElement(name = "vorgehend", namespace = CaseLawLdml.RIS_NS)
  private Rechtszug.Vorgehend vorgehend;

  @XmlElement(name = "nachgehend", namespace = CaseLawLdml.RIS_NS)
  private Rechtszug.Nachgehend nachgehend;

  @XmlElement(name = "norm", namespace = CaseLawLdml.RIS_NS)
  private Norm norm;

  @XmlElement(name = "fundstelle", namespace = CaseLawLdml.RIS_NS)
  private Fundstelle fundstelle;

  @XmlElement(name = "fundstelleLiteraturUnselbststaendig", namespace = CaseLawLdml.RIS_NS)
  private Fundstelle fundstelleLiteraturUnselbststaendig;
}
