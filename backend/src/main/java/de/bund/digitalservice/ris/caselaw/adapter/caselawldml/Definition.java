package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Definition {

  /** The term that is defined in this case law e.g. "indirekte Steuern" or "Sachgesamtheit". */
  @XmlAttribute(name = "definedTerm", namespace = CaseLawLdml.RIS_NS, required = true)
  private String definedTerm;

  /**
   * Optional: The border number of this case law where the definition of the term can be found.
   * Example: In border number 3 of this document the term "indirekte Steuern" is defined.
   */
  @XmlAttribute(name = "definingBorderNumber", namespace = CaseLawLdml.RIS_NS, required = false)
  private Long definingBorderNumber;
}
