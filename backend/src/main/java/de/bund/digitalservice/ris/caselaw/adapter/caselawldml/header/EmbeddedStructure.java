package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlMixed;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@XmlRootElement(name = "embeddedStructure", namespace = CaseLawLdml.AKN_NS)
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
@AllArgsConstructor
public class EmbeddedStructure {
  @XmlMixed
  @XmlAnyElement(lax = true)
  @XmlElementRefs({
    @XmlElementRef(name = "p", namespace = CaseLawLdml.AKN_NS, type = DocTitle.class)
  })
  private List<Object> content = new ArrayList<>();
}
