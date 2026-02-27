package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Header;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody.JudgmentBody;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Meta;
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
public class Judgment {
  @Builder.Default
  @XmlAttribute(name = "name")
  private String name = "";

  @XmlElement(name = "meta", namespace = CaseLawLdml.AKN_NS)
  private Meta meta;

  @XmlElement(name = "header", namespace = CaseLawLdml.AKN_NS)
  private Header header;

  @XmlElement(name = "judgmentBody", namespace = CaseLawLdml.AKN_NS)
  private JudgmentBody judgmentBody;
}
