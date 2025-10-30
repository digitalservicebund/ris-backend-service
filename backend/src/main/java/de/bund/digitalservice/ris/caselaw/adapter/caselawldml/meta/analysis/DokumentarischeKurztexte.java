package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.JaxbHtml;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@XmlRootElement(name = "dokumentarischeKurztexte", namespace = CaseLawLdml.RIS_NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class DokumentarischeKurztexte {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Dokumentarische Kurztexte";

  @XmlElement(name = "entscheidungsnamen", namespace = CaseLawLdml.RIS_NS)
  private Entscheidungsnamen entscheidungsnamen;

  @XmlElement(name = "titelzeile", namespace = CaseLawLdml.RIS_NS)
  private JaxbHtml titelzeile;

  @XmlElement(name = "orientierungssatz", namespace = CaseLawLdml.RIS_NS)
  private JaxbHtml orientierungssatz;

  @XmlElement(name = "sonstigerOrientierungssatz", namespace = CaseLawLdml.RIS_NS)
  private JaxbHtml sonstigerOrientierungssatz;
}
