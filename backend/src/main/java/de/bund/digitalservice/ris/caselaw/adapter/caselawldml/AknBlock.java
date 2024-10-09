package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import jakarta.xml.bind.annotation.XmlSeeAlso;
import lombok.NoArgsConstructor;
import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorNode;

@NoArgsConstructor
@XmlDiscriminatorNode("@name")
@XmlSeeAlso({Title.class, Opinions.class})
public abstract class AknBlock {
  public abstract String getName();
}
