package de.bund.digitalservice.ris.utils;

import de.bund.digitalservice.ris.datamodel.docx.DocUnitDocx;
import de.bund.digitalservice.ris.datamodel.docx.DocUnitRandnummer;
import de.bund.digitalservice.ris.datamodel.docx.DocUnitTable;
import de.bund.digitalservice.ris.datamodel.docx.DocUnitTextElement;
import jakarta.xml.bind.JAXBElement;
import java.util.stream.Collectors;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;

public class DocUnitDocxBuilder {
  private P paragraph;
  private Tbl table;

  private DocUnitDocxBuilder() {}

  public static DocUnitDocxBuilder newInstance() {
    return new DocUnitDocxBuilder();
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
    } else if (isText()) {
      return convertToTextElement();
    }

    return null;
  }

  private boolean isRandnummer() {
    var isText = isText();

    if (isText) {
      if (paragraph.getPPr() != null) {
        if (paragraph.getPPr().getPStyle() != null) {
          return paragraph.getPPr().getPStyle().getVal().equals("RandNummer");
        }
      }
    }

    return false;
  }

  private DocUnitRandnummer convertToRandnummer() {
    DocUnitRandnummer randnummer = new DocUnitRandnummer();

    paragraph.getContent().stream()
        .filter(part -> part instanceof R)
        .map(part -> (R) part)
        .forEach(r -> randnummer.addNumberText(parseTextFromRun(r)));

    return randnummer;
  }

  private boolean isText() {
    if (paragraph == null) {
      return false;
    }

    var hasRElement = paragraph.getContent().stream().anyMatch(tag -> tag instanceof R);

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

  private DocUnitTextElement convertToTextElement() {
    var textElement = new DocUnitTextElement();

    var pPr = paragraph.getPPr();
    if (pPr != null) {
      var jc = pPr.getJc();
      if (jc != null) {
        if (jc.getVal() == JcEnumeration.CENTER) {
          textElement.setAlignment("center");
        }
      }

      var rPr = pPr.getRPr();
      if (rPr != null) {
        if (rPr.getB() != null) {
          textElement.setBold(rPr.getB().isVal());
        }

        if (rPr.getSz() != null) {
          textElement.setSize(rPr.getSz().getVal());
        }
      }
    }

    paragraph.getContent().stream()
        .filter(part -> part instanceof R)
        .map(part -> (R) part)
        .forEach(r -> textElement.addText(parseTextFromRun(r)));

    return textElement;
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
