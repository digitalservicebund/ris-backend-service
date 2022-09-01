package de.bund.digitalservice.ris.domain.docx;

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
    NumberingList.DocUnitNumberingListNumberFormat numberFormat,
    String iLvl,
    JcEnumeration lvlJc,
    String suff) {}
