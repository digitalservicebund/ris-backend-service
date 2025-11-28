package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Classification {
  @Builder.Default
  @XmlAttribute(name = "source")
  private String source = CaseLawLdml.RIS_REF;

  @XmlElement(name = "keyword", namespace = CaseLawLdml.AKN_NS)
  private List<Keyword> keyword;
}
