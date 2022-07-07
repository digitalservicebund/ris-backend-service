package de.bund.digitalservice.ris.utils;

import de.bund.digitalservice.ris.domain.docx.DocUnitDocx;
import jakarta.xml.bind.JAXBElement;
import java.util.Map;
import org.docx4j.model.listnumbering.ListNumberingDefinition;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.P;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tbl;

public class DocxConverter {
  Map<String, Style> styles;
  Map<String, BinaryPartAbstractImage> images;
  Map<String, ListNumberingDefinition> listNumberingDefinitions;

  public void setImages(Map<String, BinaryPartAbstractImage> images) {
    this.images = images;
  }

  public void setStyles(Map<String, Style> styles) {
    this.styles = styles;
  }

  public void setNumbering(Map<String, ListNumberingDefinition> listNumberingDefinitions) {
    this.listNumberingDefinitions = listNumberingDefinitions;
  }

  public DocUnitDocx convert(Object part) {
    if (part instanceof P p) {
      return convertP(p);
    } else if (part instanceof JAXBElement<?> element && element.getDeclaredType() == Tbl.class) {
      return convertTbl((Tbl) element.getValue());
    }

    return new DocUnitDocx() {
      @Override
      public String toString() {
        return part.getClass().getName();
      }

      @Override
      public String toHtmlString() {
        return "<p><span style=\"color: #FF0000;\">" + part.getClass().getName() + "</span></p>";
      }
    };
  }

  private DocUnitDocx convertP(P part) {
    var builder =
        DocUnitDocxBuilder.newInstance()
            .useStyles(styles)
            .useImages(images)
            .useListNumberingDefinitions(listNumberingDefinitions)
            .setParagraph(part);

    return builder.build();
  }

  private DocUnitDocx convertTbl(Tbl part) {
    var builder =
        DocUnitDocxBuilder.newInstance().useStyles(styles).useImages(images).setTable(part);

    return builder.build();
  }
}
