package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@XmlAccessorType(XmlAccessType.FIELD)
public class OtherAnalysis {
  @Builder.Default
  @XmlAttribute(name = "source")
  private String source = CaseLawLdml.RIS_REF;

  @XmlElement(name = "dokumentarischeKurztexte", namespace = CaseLawLdml.RIS_NS)
  private DokumentarischeKurztexte dokumentarischeKurztexte;
}
