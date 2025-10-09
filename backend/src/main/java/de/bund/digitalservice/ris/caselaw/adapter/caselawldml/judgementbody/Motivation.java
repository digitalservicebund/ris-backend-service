package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.BaseLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@XmlRootElement(name = "motivation", namespace = CaseLawLdml.AKN_NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class Motivation extends BaseLdml {
  @XmlElementRefs({
    @XmlElementRef(name = "block", namespace = CaseLawLdml.AKN_NS, type = Block.class)
  })
  @XmlAnyElement(lax = true)
  private List<Object> content = new ArrayList<>();
}
