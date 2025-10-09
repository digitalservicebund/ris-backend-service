package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@XmlRootElement(name = "documentRef", namespace = CaseLawLdml.AKN_NS)
public class DocumentRef {
  @XmlAttribute(name = "href")
  private String href;

  @XmlAttribute(name = "name")
  private String name;

  @XmlAttribute(name = "showAs")
  private String showAs;
}
