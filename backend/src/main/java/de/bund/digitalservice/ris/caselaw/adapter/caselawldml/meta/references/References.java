package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.references;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.TlcElement;
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
public class References {
  @Builder.Default
  @XmlAttribute(name = "source")
  private String source = CaseLawLdml.RIS_REF;

  @XmlElement(name = "TLCOrganization", namespace = CaseLawLdml.AKN_NS)
  List<TlcElement> tlcOrganizations;

  @XmlElement(name = "TLCPerson", namespace = CaseLawLdml.AKN_NS)
  List<TlcElement> tlcPersons;

  @XmlElement(name = "TLCLocation", namespace = CaseLawLdml.AKN_NS)
  List<TlcElement> tlcLocations;
}
