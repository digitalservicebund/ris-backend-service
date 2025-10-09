package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification;

import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class FrbrThis {
  @XmlAttribute private String value;
}
