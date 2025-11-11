package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Analysis {
  @XmlAttribute(name = "source")
  private static final String SOURCE = CaseLawLdml.RIS_REF;

  @XmlElement(name = "otherReferences", namespace = CaseLawLdml.AKN_NS)
  private OtherReferences otherReferences;

  @XmlElement(name = "otherAnalysis", namespace = CaseLawLdml.AKN_NS)
  private OtherAnalysis otherAnalysis;

  @XmlTransient
  public boolean isEmpty() {
    return otherAnalysis == null || otherAnalysis.isEmpty();
  }
}
