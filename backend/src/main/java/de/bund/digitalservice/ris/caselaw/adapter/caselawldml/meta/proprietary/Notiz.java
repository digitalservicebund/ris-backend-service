package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Notiz {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Notiz";

  @XmlValue private String content;
}
