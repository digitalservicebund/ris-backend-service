package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.JaxbHtml;
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
public class JudgmentBody {

  @XmlElement(name = "introduction", namespace = CaseLawLdml.AKN_NS)
  private List<Introduction> introductions;

  @XmlElement(name = "decision", namespace = CaseLawLdml.AKN_NS)
  private JaxbHtml decision;

  @XmlElement(name = "motivation", namespace = CaseLawLdml.AKN_NS)
  private List<Motivation> motivations;
}
