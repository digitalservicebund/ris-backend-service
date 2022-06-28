package de.bund.digitalservice.ris.utils;

import de.bund.digitalservice.ris.domain.docx.DocUnitDocx;
import de.bund.digitalservice.ris.domain.docx.DocUnitParagraphTextElement;
import de.bund.digitalservice.ris.domain.docx.DocUnitRandnummer;
import de.bund.digitalservice.ris.domain.docx.DocUnitRunTextElement;
import de.bund.digitalservice.ris.domain.docx.DocUnitTable;
import de.bund.digitalservice.ris.domain.docx.DocUnitTextElement;
import jakarta.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.docx4j.wml.Jc;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPrAbstract;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.docx4j.wml.UnderlineEnumeration;

public class DocUnitDocxBuilder {
  P paragraph;
  Tbl table;
  Map<String, Style> styles = new HashMap<>();

  private DocUnitDocxBuilder() {}

  public static DocUnitDocxBuilder newInstance() {
    return new DocUnitDocxBuilder();
  }

  public DocUnitDocxBuilder setStyles(Map<String, Style> styles) {
    this.styles = styles;

    return this;
  }

  public DocUnitDocxBuilder setParagraph(P paragraph) {
    this.paragraph = paragraph;

    return this;
  }

  public DocUnitDocxBuilder setTable(Tbl table) {
    this.table = table;

    return this;
  }

  public DocUnitDocx build() {
    if (isTable()) {
      return convertToTable();
    }

    if (isRandnummer()) {
      return convertToRandnummer();
      //    } else if (isImage()) {
      //      return convertToImageElement();
    } else if (isText()) {
      return convertToParagraphTextElement();
    }

    return null;
  }

  private boolean isRandnummer() {
    var isText = isText();

    if (isText && paragraph.getPPr() != null && paragraph.getPPr().getPStyle() != null) {
      return paragraph.getPPr().getPStyle().getVal().equals("RandNummer");
    }

    return false;
  }

  private DocUnitRandnummer convertToRandnummer() {
    DocUnitRandnummer randnummer = new DocUnitRandnummer();

    paragraph.getContent().stream()
        .filter(R.class::isInstance)
        .map(R.class::cast)
        .forEach(r -> randnummer.addNumberText(parseTextFromRun(r)));

    return randnummer;
  }

  private boolean isText() {
    if (paragraph == null) {
      return false;
    }

    var hasRElement = paragraph.getContent().stream().anyMatch(R.class::isInstance);

    if (!hasRElement) {
      return paragraph.getPPr() != null;
    }

    return paragraph.getContent().stream()
        .anyMatch(
            tag -> {
              if (tag instanceof R r) {
                return r.getContent().stream()
                    .anyMatch(
                        subTag -> {
                          if (subTag instanceof JAXBElement<?> element) {
                            return element.getDeclaredType() == Text.class;
                          }

                          return false;
                        });
              }

              return false;
            });
  }

  private DocUnitParagraphTextElement convertToParagraphTextElement() {
    var textElement = new DocUnitParagraphTextElement();

    var pPr = paragraph.getPPr();
    String alignment = getAlignment(pPr);
    if (alignment != null) {
      textElement.setAlignment(alignment);
    }

    addParagraphStyle(textElement, pPr);

    paragraph.getContent().stream()
        .filter(R.class::isInstance)
        .map(R.class::cast)
        .forEach(
            r -> {
              var text = parseTextElementFromRun(r);
              if (text != null) {
                textElement.addRunTextElement(text);
              }
            });

    return textElement;
  }

  //  private boolean isImage() {
  //    if (paragraph == null) {
  //      return false;
  //    }
  //
  //    return paragraph.getContent().stream()
  //        .anyMatch(
  //            tag -> {
  //              if (tag instanceof R r) {
  //                return r.getContent().stream()
  //                    .anyMatch(
  //                        subTag -> {
  //                          if (subTag instanceof JAXBElement<?> element) {
  //                            return element.getDeclaredType() == Drawing.class;
  //                          }
  //
  //                          return false;
  //                        });
  //              }
  //
  //              return false;
  //            });
  //  }
  //
  //  private DocUnitDocx convertToImageElement() {
  //    paragraph.getContent().stream()
  //        .filter(R.class::isInstance)
  //        .map(R.class::cast)
  //        .map(run -> {
  //          return
  // run.getContent().stream().filter(Drawing.class::isInstance).map(Drawing.class::cast);
  //        })
  //        .count();
  //    return null;
  //  }

  private String getAlignment(PPr pPr) {
    if (pPr == null) {
      return null;
    }

    Jc jc = null;

    var pStyle = pPr.getPStyle();
    if (pStyle != null && pStyle.getVal() != null) {
      Style style = styles.get(pStyle.getVal());
      if (style != null && style.getPPr() != null) {
        jc = style.getPPr().getJc();
      }
    }

    if (pPr.getJc() != null) {
      jc = pPr.getJc();
    }

    if (jc != null && jc.getVal() != null) {
      switch (jc.getVal()) {
        case CENTER:
          return "center";
      }
    }

    return null;
  }

  private void addParagraphStyle(DocUnitTextElement textElement, PPr pPr) {
    if (pPr == null) {
      return;
    }

    RPrAbstract styleRPr = null;
    var pStyle = pPr.getPStyle();
    if (pStyle != null && pStyle.getVal() != null) {
      var style = styles.get(pStyle.getVal());
      if (style != null) {
        styleRPr = style.getRPr();
      }
    }

    textElement.setBold(isBold(styleRPr, pPr.getRPr()));

    var size = getSize(styleRPr, pPr.getRPr());
    if (size != null) {
      textElement.setSize(size);
    }

    var underline = getUnderline(styleRPr, pPr.getRPr());
    if (underline != null) {
      textElement.setUnderline(underline);
    }
  }

  private boolean isBold(RPrAbstract styleRPr, RPrAbstract rPr) {
    if (styleRPr == null && rPr == null) {
      return false;
    }

    boolean bold = false;
    if (styleRPr != null && styleRPr.getB() != null) {
      bold = styleRPr.getB().isVal();
    }

    if (rPr != null && rPr.getB() != null && rPr.getB().isVal()) {
      bold = rPr.getB().isVal();
    }

    return bold;
  }

  private BigInteger getSize(RPrAbstract styleRPr, RPrAbstract rPr) {
    if (styleRPr == null && rPr == null) {
      return null;
    }

    BigInteger size = null;
    if (styleRPr != null && styleRPr.getSz() != null && styleRPr.getSz().getVal() != null) {
      size = styleRPr.getSz().getVal();
    }

    if (rPr != null && rPr.getSz() != null && rPr.getSz().getVal() != null) {
      size = rPr.getSz().getVal();
    }

    return size;
  }

  private String getUnderline(RPrAbstract styleRPr, RPrAbstract rPr) {
    if (styleRPr == null && rPr == null) {
      return null;
    }

    UnderlineEnumeration underline = null;
    if (styleRPr != null && styleRPr.getU() != null && styleRPr.getU().getVal() != null) {
      underline = styleRPr.getU().getVal();
    }

    if (rPr != null && rPr.getU() != null && rPr.getU().getVal() != null) {
      underline = rPr.getU().getVal();
    }

    if (underline != null) {
      switch (underline) {
        case SINGLE:
          return "single";
      }
    }

    return null;
  }

  private void addStyle(DocUnitTextElement textElement, RPrAbstract rPr) {
    if (rPr == null) {
      return;
    }

    if (rPr.getB() != null && rPr.getB().isVal()) {
      textElement.setBold(rPr.getB().isVal());
    }

    if (rPr.getSz() != null) {
      textElement.setSize(rPr.getSz().getVal());
    }

    if (rPr.getU() != null && rPr.getU().getVal() == UnderlineEnumeration.SINGLE) {
      textElement.setUnderline("single");
    }
  }

  private DocUnitRunTextElement parseTextElementFromRun(R r) {
    var runTextElement = new DocUnitRunTextElement();

    var text = parseTextFromRun(r);
    if (!text.isEmpty()) {
      runTextElement.setText(text);

      addStyle(runTextElement, r.getRPr());

      return runTextElement;
    }

    return null;
  }

  private String parseTextFromRun(R r) {
    return r.getContent().stream()
        .filter(part -> part instanceof JAXBElement<?>)
        .map(part -> (JAXBElement<?>) part)
        .filter(el -> el.getDeclaredType() == Text.class)
        .map(el -> (Text) el.getValue())
        .map(Text::getValue)
        .collect(Collectors.joining());
  }

  private boolean isTable() {
    return table != null;
  }

  private DocUnitDocx convertToTable() {
    DocUnitTable docUnitDocx = new DocUnitTable();

    var result =
        table.getContent().stream().map(this::convertTableElements).collect(Collectors.joining());
    docUnitDocx.setTextContent(result);

    return docUnitDocx;
  }

  private String convertTableElements(Object tableElement) {
    if (tableElement instanceof Tr tr) {
      return tr.getContent().stream()
          .map(
              el -> {
                if (el instanceof JAXBElement<?> element) {
                  return convertTableElements(element.getValue());
                }

                return el.getClass().getName();
              })
          .collect(Collectors.joining());
    } else if (tableElement instanceof Tc tc) {
      return tc.getContent().stream().map(this::convertTableElements).collect(Collectors.joining());
    } else if (tableElement instanceof P p) {
      return p.getContent().stream().map(this::convertTableElements).collect(Collectors.joining());
    } else if (tableElement instanceof R r) {
      return r.getContent().stream()
          .map(
              el -> {
                if (el instanceof JAXBElement<?> element) {
                  return convertTableElements(element.getValue());
                }

                return el.getClass().getName();
              })
          .collect(Collectors.joining());
    } else if (tableElement instanceof Text text) {
      return text.getValue();
    }

    return "<no table elements found>";
  }
}
