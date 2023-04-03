package de.bund.digitalservice.ris.caselaw.adapter.converter.docx;

import de.bund.digitalservice.ris.caselaw.domain.docx.DocumentUnitDocx;
import de.bund.digitalservice.ris.caselaw.domain.docx.DocxImagePart;
import java.util.HashMap;
import java.util.Map;
import org.docx4j.model.listnumbering.ListNumberingDefinition;
import org.docx4j.wml.Style;

public abstract class DocxBuilder {
  protected Map<String, Style> styles = new HashMap<>();
  protected Map<String, DocxImagePart> images = new HashMap<>();
  protected Map<String, ListNumberingDefinition> listNumberingDefinitions;

  public DocxBuilder useStyles(Map<String, Style> styles) {
    this.styles = styles;

    return this;
  }

  public DocxBuilder useImages(Map<String, DocxImagePart> images) {
    this.images = images;

    return this;
  }

  public DocxBuilder useListNumberingDefinitions(
      Map<String, ListNumberingDefinition> listNumberingDefinitions) {
    this.listNumberingDefinitions = listNumberingDefinitions;

    return this;
  }

  public abstract DocumentUnitDocx build();
}
