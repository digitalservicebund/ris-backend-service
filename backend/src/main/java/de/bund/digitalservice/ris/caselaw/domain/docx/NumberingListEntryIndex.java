package de.bund.digitalservice.ris.caselaw.domain.docx;

import de.bund.digitalservice.ris.caselaw.domain.docx.NumberingList.DocumentUnitNumberingListNumberFormat;
import org.docx4j.wml.JcEnumeration;

public record NumberingListEntryIndex(
    String lvlText,
    String startVal,
    String restartNumberingAfterBreak,
    String color,
    String fontStyle,
    String fontSize,
    boolean lvlPicBullet,
    boolean isLgl,
    DocumentUnitNumberingListNumberFormat numberFormat,
    String iLvl,
    JcEnumeration lvlJc,
    String suff) {}
