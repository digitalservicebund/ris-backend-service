package de.bund.digitalservice.ris.caselaw.adapter.converter.docx;

import de.bund.digitalservice.ris.caselaw.domain.docx.AnchorImageElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.ParagraphElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.RunElement;
import java.util.List;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.Style;

/**
 * Converter to convert docx4j {@link org.docx4j.wml.P} element to internal used {@link
 * ParagraphElement}
 */
public class ParagraphConverter {
  private ParagraphConverter() {}

  /**
   * Convert the {@link org.docx4j.wml.P} to a {@link ParagraphElement}
   *
   * @see DocxConverter
   * @param paragraph original docx4j {@link org.docx4j.wml.P} element
   * @param converter reference to the converter
   * @return the internal representation as {@link ParagraphElement}
   */
  public static ParagraphElement convert(P paragraph, DocxConverter converter) {
    if (paragraph == null) {
      return null;
    }

    var paragraphElement = new ParagraphElement();

    PPr paragraphProperties = paragraph.getPPr();
    if (paragraphProperties != null) {
      if (paragraphProperties.getPStyle() != null) {
        paragraphElement.setStyleReference(paragraphProperties.getPStyle().getVal());
      }
      // TODO identify multiple levels of indentation
      if (paragraphProperties.getInd() != null && paragraphProperties.getInd().getLeft() != null) {
        paragraphElement.addStyle("margin-left", "40px");
      }

      String alignment = getAlignment(paragraphProperties, converter);
      if (alignment != null) {
        paragraphElement.setAlignment(alignment);
      }
    }

    paragraph.getContent().stream()
        .filter(R.class::isInstance)
        .map(R.class::cast)
        .forEach(run -> RunElementConverter.convert(run, paragraphElement, converter));

    sortParagraphElements(paragraphElement);

    return paragraphElement;
  }

  private static String getAlignment(PPr pPr, DocxConverter converter) {
    if (pPr == null) {
      return null;
    }

    Jc jc = null;

    var pStyle = pPr.getPStyle();
    if (pStyle != null && pStyle.getVal() != null) {
      Style style = converter.getStyles().get(pStyle.getVal());
      if (style != null && style.getPPr() != null) {
        jc = style.getPPr().getJc();
      }
    }

    if (pPr.getJc() != null) {
      jc = pPr.getJc();
    }

    if (jc != null && jc.getVal() != null && jc.getVal() == JcEnumeration.CENTER) {
      return "center";
    }

    if (jc != null && jc.getVal() != null && jc.getVal() == JcEnumeration.RIGHT) {
      return "right";
    }

    if (jc != null && jc.getVal() != null && jc.getVal() == JcEnumeration.LEFT) {
      return "left";
    }

    if (jc != null && jc.getVal() != null && jc.getVal() == JcEnumeration.BOTH) {
      return "justify";
    }

    return null;
  }

  private static void sortParagraphElements(ParagraphElement paragraphElement) {
    List<RunElement> sortedList =
        paragraphElement.getRunElements().stream()
            .sorted(
                (o1, o2) -> {
                  if (o1.getClass().equals(o2.getClass())) {
                    return 0;
                  }

                  if (o1 instanceof AnchorImageElement && !(o2 instanceof AnchorImageElement)) {
                    return -1;
                  }

                  if (!(o1 instanceof AnchorImageElement) && o2 instanceof AnchorImageElement) {
                    return 1;
                  }

                  return 0;
                })
            .toList();

    paragraphElement.setRunElements(sortedList);
  }
}
