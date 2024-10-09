package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Motivation {

  // Jaxb doesn't handle elements with the same name very well.
  // They need to be in a list with the same class instead of two separate fields.
  // A Map is used with @XmlElement added to a helper getter/setter to facilitate easier use.
  @XmlTransient private Map<String, AknEmbeddedStructureInBlock> blocks;

  @XmlElement(name = "block", namespace = CaseLawLdml.AKN_NS)
  public void setJaxbBlocks(List<AknEmbeddedStructureInBlock> blocks) {
    for (AknEmbeddedStructureInBlock block : blocks) {
      this.blocks.put(block.getName(), block);
    }
  }

  @XmlElement(name = "block", namespace = CaseLawLdml.AKN_NS)
  public List<AknEmbeddedStructureInBlock> getJaxbBlocks() {
    return new ArrayList<>(blocks.values());
  }

  public Motivation() {
    this.blocks = new HashMap<>();
  }

  public Motivation withBlock(String name, AknEmbeddedStructureInBlock block) {
    if (block != null) {
      blocks.put(name, block);
    }
    return this;
  }

  public AknEmbeddedStructureInBlock getBlock(String name) {
    return blocks.get(name);
  }

  public JaxbHtml getHtml(String name) {
    AknEmbeddedStructureInBlock block = blocks.get(name);
    if (block == null) {
      return null;
    } else {
      return block.getContent();
    }
  }
}
