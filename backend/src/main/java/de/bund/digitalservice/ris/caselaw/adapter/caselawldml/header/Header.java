package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@XmlRootElement(name = "header", namespace = CaseLawLdml.AKN_NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class Header {
  @XmlElement(name = "p", namespace = CaseLawLdml.AKN_NS)
  private List<Paragraph> paragraphs = new ArrayList<>();
}
