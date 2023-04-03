package de.bund.digitalservice.ris.caselaw.adapter.converter.docx;

import de.bund.digitalservice.ris.caselaw.domain.Converter;
import de.bund.digitalservice.ris.caselaw.domain.docx.DocumentUnitDocx;
import de.bund.digitalservice.ris.caselaw.domain.docx.DocxImagePart;
import de.bund.digitalservice.ris.caselaw.domain.docx.ErrorElement;
import jakarta.xml.bind.JAXBElement;
import java.util.Map;
import org.docx4j.model.listnumbering.ListNumberingDefinition;
import org.docx4j.wml.P;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tbl;

public class DocxConverter implements Converter<DocumentUnitDocx> {
  Map<String, Style> styles;
  Map<String, DocxImagePart> images;
  Map<String, ListNumberingDefinition> listNumberingDefinitions;

  public void setImages(Map<String, DocxImagePart> images) {
    this.images = images;
  }

  public void setStyles(Map<String, Style> styles) {
    this.styles = styles;
  }

  public void setNumbering(Map<String, ListNumberingDefinition> listNumberingDefinitions) {
    this.listNumberingDefinitions = listNumberingDefinitions;
  }

  public DocumentUnitDocx convert(Object part) {
    DocxBuilder builder;
    if (part instanceof P p) {
      builder = convertP(p);
    } else if (part instanceof JAXBElement<?> element && element.getDeclaredType() == Tbl.class) {
      builder = convertTbl((Tbl) element.getValue());
    } else {
      return new ErrorElement(part.getClass().getName());
    }

    builder
        .useStyles(styles)
        .useImages(images)
        .useListNumberingDefinitions(listNumberingDefinitions);

    return builder.build();
  }

  private DocxBuilder convertP(P part) {
    return DocumentUnitDocxBuilder.newInstance().setParagraph(part);
  }

  private DocxBuilder convertTbl(Tbl part) {
    return DocxTableBuilder.newInstance().setTable(part);
  }
}
