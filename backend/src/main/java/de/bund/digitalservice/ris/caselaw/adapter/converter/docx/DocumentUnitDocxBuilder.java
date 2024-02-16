package de.bund.digitalservice.ris.caselaw.adapter.converter.docx;

import de.bund.digitalservice.ris.caselaw.domain.docx.BorderNumber;
import de.bund.digitalservice.ris.caselaw.domain.docx.DocumentUnitDocx;
import de.bund.digitalservice.ris.caselaw.domain.docx.NumberingList.DocumentUnitNumberingListNumberFormat;
import de.bund.digitalservice.ris.caselaw.domain.docx.NumberingListEntry;
import de.bund.digitalservice.ris.caselaw.domain.docx.NumberingListEntryIndex;
import jakarta.xml.bind.JAXBElement;
import java.util.List;
import org.docx4j.model.listnumbering.AbstractListNumberingDefinition;
import org.docx4j.model.listnumbering.ListLevel;
import org.docx4j.model.listnumbering.ListNumberingDefinition;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.NumPr;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;

public class DocumentUnitDocxBuilder extends DocxBuilder {
  P paragraph;

  private DocumentUnitDocxBuilder() {}

  public static DocumentUnitDocxBuilder newInstance() {
    return new DocumentUnitDocxBuilder();
  }

  public DocumentUnitDocxBuilder setParagraph(P paragraph) {
    this.paragraph = paragraph;

    return this;
  }

  public DocumentUnitDocx build() {
    if (isBorderNumber()) {
      return convertToBorderNumber();
    } else if (isNumberingListEntry()) {
      return convertToNumberingListEntry();
    } else if (isParagraph()) {
      return ParagraphConverter.convert(paragraph, converter);
    }

    return null;
  }

  private boolean isText() {
    if (!isParagraph()) {
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

  private boolean isBorderNumber() {
    if (!isText()) {
      return false;
    }

    PPr ppr = paragraph.getPPr();
    if (ppr == null) {
      return false;
    }

    if (ppr.getPStyle() != null
        && ppr.getPStyle().getVal().equals("Listenabsatz")
        && ppr.getFramePr() != null) {
      return true;
    }

    if (ppr.getPStyle() != null
        && List.of("RandNummer", "ListParagraph").contains(ppr.getPStyle().getVal())) {
      return true;
    }

    // Found in some BGH documents: the border numbers have no dedicated style element in
    // document.xml. So we decided to combine 3 characteristics that hopefully match these kind
    // of border numbers in all cases
    // --> keepNext exists (=true), line in spacing to be 240 and the text to be only one integer
    return ppr.getKeepNext() != null
        && ppr.getKeepNext().isVal()
        && ppr.getSpacing() != null
        && ppr.getSpacing().getLine() != null
        && ppr.getSpacing().getLine().intValue() == 240
        && contentIsOnlyInteger();
  }

  private boolean contentIsOnlyInteger() {
    StringBuilder content = new StringBuilder();
    paragraph.getContent().stream()
        .filter(R.class::isInstance)
        .map(R.class::cast)
        .forEach(r -> content.append(RunElementConverter.parseTextFromRun(r)));
    try {
      Integer.parseInt(content.toString());
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private BorderNumber convertToBorderNumber() {
    BorderNumber borderNumber = new BorderNumber();

    paragraph.getContent().stream()
        .filter(R.class::isInstance)
        .map(R.class::cast)
        .forEach(r -> borderNumber.addNumberText(RunElementConverter.parseTextFromRun(r)));

    PPr ppr = paragraph.getPPr();
    if (ppr != null && ppr.getNumPr() != null && ppr.getNumPr().getNumId() != null) {
      borderNumber.setNumId(paragraph.getPPr().getNumPr().getNumId().getVal().intValue());
    }
    return borderNumber;
  }

  private boolean isNumberingListEntry() {
    if (!isParagraph() || paragraph.getPPr() == null) {
      return false;
    }

    return paragraph.getPPr().getNumPr() != null;
  }

  private NumberingListEntry convertToNumberingListEntry() {
    NumPr numPr = paragraph.getPPr().getNumPr();
    String numId = null;
    String iLvl = null;

    ListNumberingDefinition listNumberingDefinition = null;
    if (numPr != null && numPr.getNumId() != null && numPr.getNumId().getVal() != null) {
      numId = numPr.getNumId().getVal().toString();
      listNumberingDefinition = converter.getListNumberingDefinitions().get(numId);
    }

    if (numPr != null && numPr.getIlvl() != null && numPr.getIlvl().getVal() != null) {
      iLvl = numPr.getIlvl().getVal().toString();
    }

    // Unless we find a counter example, we treat the presence of a numPr element with numId 0
    // as a list entry that brings its own numbering symbol inside the paragraph part --> we don't
    // add any own numbering symbols
    DocumentUnitNumberingListNumberFormat numberFormat =
        numId != null && numId.equals("0")
            ? DocumentUnitNumberingListNumberFormat.NONE
            : DocumentUnitNumberingListNumberFormat.BULLET;

    NumberingListEntryIndex numberingListEntryIndex =
        new NumberingListEntryIndex(
            "", "1", "", "", "", "", false, false, numberFormat, iLvl, JcEnumeration.RIGHT, "tab");
    if (listNumberingDefinition != null) {
      AbstractListNumberingDefinition abstractListDefinition =
          listNumberingDefinition.getAbstractListDefinition();

      if (abstractListDefinition != null && iLvl != null) {
        ListLevel listLevel = abstractListDefinition.getListLevels().get(iLvl);

        if (listLevel != null) {
          numberingListEntryIndex = setNumberingListEntryIndex(listLevel, iLvl);
        }
      }
    }

    return new NumberingListEntry(
        ParagraphConverter.convert(paragraph, converter), numberingListEntryIndex);
  }

  private NumberingListEntryIndex setNumberingListEntryIndex(ListLevel listLevel, String iLvl) {
    return NumberingListEntryIndexGenerator.generate(listLevel, iLvl);
  }

  private boolean isParagraph() {
    return paragraph != null;
  }
}
