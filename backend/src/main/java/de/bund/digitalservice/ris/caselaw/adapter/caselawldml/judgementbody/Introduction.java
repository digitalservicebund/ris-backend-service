package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.BaseLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
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
@XmlRootElement(name = "introduction", namespace = CaseLawLdml.AKN_NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class Introduction extends BaseLdml {
  @XmlAnyElement private List<Object> content = new ArrayList<>();
}
