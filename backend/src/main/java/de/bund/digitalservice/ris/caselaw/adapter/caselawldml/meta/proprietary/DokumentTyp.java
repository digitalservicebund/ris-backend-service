package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class DokumentTyp {
  @XmlValue private String value;

  @XmlAttribute(namespace = CaseLawLdml.AKN_NS, name = "eId")
  private static final String E_ID = "dokumenttyp";

  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Dokumenttyp";
}
