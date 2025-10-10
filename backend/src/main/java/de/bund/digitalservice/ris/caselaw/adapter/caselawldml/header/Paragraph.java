package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
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

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@XmlRootElement(name = "p", namespace = CaseLawLdml.AKN_NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class Paragraph {
  @XmlMixed
  @XmlElementRefs({
    @XmlElementRef(name = "docNumber", namespace = CaseLawLdml.AKN_NS, type = DocNumber.class),
    @XmlElementRef(name = "docDate", namespace = CaseLawLdml.AKN_NS, type = DocDate.class),
    @XmlElementRef(name = "courtType", namespace = CaseLawLdml.AKN_NS, type = CourtType.class),
    @XmlElementRef(name = "docType", namespace = CaseLawLdml.AKN_NS, type = DocType.class),
    @XmlElementRef(name = "docTitle", namespace = CaseLawLdml.AKN_NS, type = DocTitle.class),
    @XmlElementRef(name = "shortTitle", namespace = CaseLawLdml.AKN_NS, type = ShortTitle.class)
  })
  private List<Object> content = new ArrayList<>();
}
