package de.bund.digitalservice.ris.utils;

import de.bund.digitalservice.ris.domain.docx.DocUnitDocx;
import jakarta.xml.bind.JAXBElement;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;

public class DocxParagraphConverter {
  private DocxParagraphConverter() {}

  public static DocUnitDocx convert(Object part) {
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
        return "<div style=\"color: #FF0000;\">" + part.getClass().getName() + "</div>";
      }
    };
  }

  private static DocUnitDocx convertP(P part) {
    var builder = DocUnitDocxBuilder.newInstance().setParagraph(part);

    return builder.build();
  }

  private static DocUnitDocx convertTbl(Tbl part) {
    var builder = DocUnitDocxBuilder.newInstance().setTable(part);

    return builder.build();
  }
}
