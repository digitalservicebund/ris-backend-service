package de.bund.digitalservice.ris.caselaw.adapter.converter.docx;

import de.bund.digitalservice.ris.caselaw.domain.docx.RunTextElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.VerticalAlign;
import org.docx4j.wml.RPrAbstract;
import org.docx4j.wml.STVerticalAlignRun;
import org.docx4j.wml.UnderlineEnumeration;

public class RunElementStyleAdapter {
  private RunElementStyleAdapter() {}

  public static void addStyles(RunTextElement textElement, RPrAbstract rPr) {
    if (rPr == null) {
      return;
    }

    if (rPr.getB() != null && rPr.getB().isVal()) {
      textElement.setBold(rPr.getB().isVal());
    }

    if (rPr.getI() != null && rPr.getI().isVal()) {
      textElement.setItalic(rPr.getI().isVal());
    }

    if (rPr.getStrike() != null && rPr.getStrike().isVal()) {
      textElement.setStrike(rPr.getStrike().isVal());
    }

    if (rPr.getVertAlign() != null && rPr.getVertAlign().getVal() != null) {
      textElement.setVertAlign(convertVertAlign(rPr.getVertAlign().getVal()));
    }

    // check this again. don't find the reason why the font-size of the
    // external table style don't override the old one
    if (rPr.getSz() != null && !textElement.containsStyle("font-size")) {
      textElement.setSize(rPr.getSz().getVal().intValue());
    }

    if (rPr.getU() != null && rPr.getU().getVal() == UnderlineEnumeration.SINGLE) {
      textElement.setUnderline("single");
    }

    if (rPr.getColor() != null) {
      textElement.setColor(rPr.getColor().getVal());
    }
  }

  private static VerticalAlign convertVertAlign(STVerticalAlignRun verticalAlignRun) {
    if (verticalAlignRun == null) {
      return null;
    }

    if (verticalAlignRun == STVerticalAlignRun.SUBSCRIPT) {
      return VerticalAlign.SUBSCRIPT;
    } else if (verticalAlignRun == STVerticalAlignRun.SUPERSCRIPT) {
      return VerticalAlign.SUPERSCRIPT;
    }

    return null;
  }
}
