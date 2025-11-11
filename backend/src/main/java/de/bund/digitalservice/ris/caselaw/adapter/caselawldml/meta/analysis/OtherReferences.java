package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
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
@XmlAccessorType(XmlAccessType.FIELD)
public class OtherReferences {
  @Builder.Default
  @XmlAttribute(name = "source")
  private String source = CaseLawLdml.RIS_REF;

  @XmlElement(name = "implicitReference", namespace = CaseLawLdml.AKN_NS)
  private List<ImplicitReference> implicitReferences;

  public boolean isEmpty() {
    return implicitReferences == null || implicitReferences.isEmpty();
  }
}
