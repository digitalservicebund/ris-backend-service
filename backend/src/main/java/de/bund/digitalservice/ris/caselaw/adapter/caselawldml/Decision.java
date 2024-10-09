package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor
@Getter
public class Decision extends JaxbHtml {

  @XmlElement(name = "block", namespace = CaseLawLdml.AKN_NS)
  private AknEmbeddedStructureInBlock.OtherLongText otherLongText;

  public Decision(String html, String otherLongText) {
    super(html);
    if (StringUtils.isBlank(html)) {
      this.setHtml(null);
    }
    this.otherLongText =
        AknEmbeddedStructureInBlock.OtherLongText.build(JaxbHtml.build(otherLongText));
  }

  public static Decision build(String html, String otherLongText) {
    // Lombok build() can't return null when one input is null
    if (StringUtils.isEmpty(html) && StringUtils.isEmpty(otherLongText)) {
      return null;
    }
    return new Decision(html, otherLongText);
  }
}
