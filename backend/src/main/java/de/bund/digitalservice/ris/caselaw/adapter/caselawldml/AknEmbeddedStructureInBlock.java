package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorNode;
import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorValue;

@NoArgsConstructor
@Getter
@XmlDiscriminatorNode("@name")
@XmlSeeAlso({
  AknEmbeddedStructureInBlock.HeadNote.class,
  AknEmbeddedStructureInBlock.OtherHeadNote.class,
  AknEmbeddedStructureInBlock.Grounds.class,
  AknEmbeddedStructureInBlock.OtherLongText.class
})
public abstract class AknEmbeddedStructureInBlock {

  @XmlElement(name = "embeddedStructure", namespace = CaseLawLdml.AKN_NS)
  protected JaxbHtml content;

  public abstract String getName();

  @NoArgsConstructor
  @XmlDiscriminatorValue(HeadNote.NAME)
  public static class HeadNote extends AknEmbeddedStructureInBlock {
    public static final String NAME = "Orientierungssatz";

    public HeadNote(JaxbHtml content) {
      this.content = content;
    }

    public static HeadNote build(JaxbHtml content) {
      return content == null ? null : new HeadNote(content);
    }

    public String getName() {
      return NAME;
    }
  }

  @NoArgsConstructor
  @XmlDiscriminatorValue(OtherHeadNote.NAME)
  public static class OtherHeadNote extends AknEmbeddedStructureInBlock {
    public static final String NAME = "Sonstiger Orientierungssatz";

    public OtherHeadNote(JaxbHtml content) {
      this.content = content;
    }

    public static OtherHeadNote build(JaxbHtml content) {
      return content == null ? null : new OtherHeadNote(content);
    }

    public String getName() {
      return NAME;
    }
  }

  @NoArgsConstructor
  @XmlDiscriminatorValue(Grounds.NAME)
  public static class Grounds extends AknEmbeddedStructureInBlock {
    public static final String NAME = "Gründe";

    public Grounds(JaxbHtml content) {
      this.content = content;
    }

    public static Grounds build(JaxbHtml content) {
      return content == null ? null : new Grounds(content);
    }

    public String getName() {
      return NAME;
    }
  }

  @NoArgsConstructor
  @XmlDiscriminatorValue(OtherLongText.NAME)
  public static class OtherLongText extends AknEmbeddedStructureInBlock {
    public static final String NAME = "Sonstiger Langtext";

    public OtherLongText(JaxbHtml content) {
      this.content = content;
    }

    public static OtherLongText build(JaxbHtml content) {
      return content == null ? null : new OtherLongText(content);
    }

    public String getName() {
      return NAME;
    }
  }
}
