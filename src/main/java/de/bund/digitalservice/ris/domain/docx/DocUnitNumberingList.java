package de.bund.digitalservice.ris.domain.docx;

import java.util.ArrayList;
import java.util.List;

public class DocUnitNumberingList implements DocUnitDocx {

  private final DocUnitNumberingListNumberFormat numberFormat;

  private final String numId;

  private final String iLvl;

  private final List<DocUnitNumberingListEntry> entries = new ArrayList<>();

  public DocUnitNumberingList(
      DocUnitNumberingListNumberFormat numberFormat, String numId, String iLvl) {
    this.numberFormat = numberFormat;
    this.numId = numId;
    this.iLvl = iLvl;
  }

  public DocUnitNumberingListNumberFormat getNumberFormat() {
    return numberFormat;
  }

  public String getNumId() {
    return numId;
  }

  public String getiLvl() {
    return iLvl;
  }

  public void addNumberingListEntry(DocUnitNumberingListEntry entry) {
    entries.add(entry);
  }

  @Override
  public String toHtmlString() {
    StringBuilder sb = new StringBuilder();
    if (numberFormat == DocUnitNumberingListNumberFormat.DECIMAL) {
      sb.append("<ol>");
    } else {
      sb.append("<ul>");
    }

    entries.forEach(entry -> sb.append(entry.toHtmlString()));

    if (numberFormat == DocUnitNumberingListNumberFormat.DECIMAL) {
      sb.append("</ol>");
    } else {
      sb.append("</ul>");
    }

    return sb.toString();
  }

  public enum DocUnitNumberingListNumberFormat {
    DECIMAL,
    BULLET
  }
}
