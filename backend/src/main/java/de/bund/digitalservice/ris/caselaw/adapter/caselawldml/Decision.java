package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import jakarta.xml.bind.annotation.XmlElement;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class Decision extends JaxbHtml {

  @XmlElement(name = "block", namespace = CaseLawLdml.AKN_NS)
  private AknEmbeddedStructureInBlock.OtherLongText otherLongText;

  public Decision(List<Object> html, List<Object> otherLongText) {
    super(html);

    if (html == null || html.isEmpty() || html.stream().allMatch(Objects::isNull)) {
      this.setHtml(null);
    }

    this.otherLongText =
        AknEmbeddedStructureInBlock.OtherLongText.build(JaxbHtml.build(otherLongText));
  }

  public static Decision build(List<Object> html, List<Object> otherLongText) {
    // Lombok build() can't return null when one input is null
    if ((html == null || html.isEmpty() || html.stream().allMatch(Objects::isNull))
        && (otherLongText == null
            || otherLongText.isEmpty()
            || otherLongText.stream().allMatch(Objects::isNull))) {

      return null;
    }

    return new Decision(html, otherLongText);
  }
}
