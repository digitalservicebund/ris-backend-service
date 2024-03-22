package de.bund.digitalservice.ris.caselaw.domain.docx;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumberingList implements DocumentUnitDocx {

  // this should be a list containing either NumberingListEntry's or NumberingList's
  // so that a tree structure that can recursively be traversed
  private final List<NumberingListEntry> entries = new ArrayList<>();
  private static final Logger LOGGER = LoggerFactory.getLogger(NumberingList.class);

  public NumberingList() {
    /*Create new instance of documentUnitNumberList*/
  }

  public void addNumberingListEntry(NumberingListEntry entry) {
    entries.add(entry);
  }

  public List<NumberingListEntry> getEntries() {
    return entries;
  }

  @Override
  public String toHtmlString() {
    StringBuilder sb = new StringBuilder();
    LinkedList<String> closeTags = new LinkedList<>();
    int[] cLvl = {-1};
    List<DocumentUnitNumberingListNumberFormat> currentNumberFormat = new ArrayList<>();

    entries.forEach(
        entry -> {
          /*Get level of list entry*/
          int lvl = stringToInt(entry.numberingListEntryIndex().iLvl(), 0);

          /*Open new List if change number format at lv 0*/
          if (shouldCreateNewList(
              cLvl[0], lvl, currentNumberFormat, entry.numberingListEntryIndex().numberFormat())) {
            while (!closeTags.isEmpty()) {
              sb.append(closeTags.removeFirst());
            }
            sb.append(getOpenListTag(entry.numberingListEntryIndex()));
            closeTags.addFirst(getCloseListTag(entry.numberingListEntryIndex().numberFormat()));
            cLvl[0] = lvl;
          }

          /* Open list/sub-list Tag*/
          while (lvl > cLvl[0]) {
            sb.append(getOpenListTag(entry.numberingListEntryIndex()));
            closeTags.addFirst(getCloseListTag(entry.numberingListEntryIndex().numberFormat()));
            cLvl[0]++;
          }

          /* Close list/sub-list Tag*/
          while (lvl < cLvl[0]) {
            sb.append(closeTags.removeFirst());
            cLvl[0]--;
          }

          if (entry.numberingListEntryIndex().isLgl()) {
            sb.append("<li style=\"list-style-type:decimal\">")
                .append(entry.toHtmlString())
                .append("</li>");
          } else {
            sb.append("<li>").append(entry.toHtmlString()).append("</li>");
          }

          if (!currentNumberFormat.isEmpty()) {
            currentNumberFormat.remove(0);
          }
          currentNumberFormat.add(entry.numberingListEntryIndex().numberFormat());
          cLvl[0] = lvl;
        });

    /* Close all list/sub-list tag when last element*/
    while (!closeTags.isEmpty()) {
      sb.append(closeTags.removeFirst());
    }
    return sb.toString();
  }

  public enum DocumentUnitNumberingListNumberFormat {
    NONE,
    DECIMAL,
    BULLET,
    UPPER_ROMAN,
    LOWER_ROMAN,
    UPPER_LETTER,
    LOWER_LETTER,
  }

  private String getOpenListTag(NumberingListEntryIndex numberingListEntryIndex) {
    DocumentUnitNumberingListNumberFormat listNumberFormat = numberingListEntryIndex.numberFormat();
    String listStyle = getListType(listNumberFormat, numberingListEntryIndex);
    if (listNumberFormat == DocumentUnitNumberingListNumberFormat.BULLET) {
      return listStyle == null ? "<ul>" : String.format("<ul style=\"%s\">", listStyle);
    } else {
      return listStyle == null ? "<ol>" : String.format("<ol style=\"%s\">", listStyle);
    }
  }

  private String getCloseListTag(DocumentUnitNumberingListNumberFormat listNumberFormat) {
    return listNumberFormat == DocumentUnitNumberingListNumberFormat.BULLET ? "</ul>" : "</ol>";
  }

  private boolean shouldCreateNewList(
      int cLvl,
      int nLvl,
      List<DocumentUnitNumberingListNumberFormat> cNumberFormat,
      DocumentUnitNumberingListNumberFormat nNumberFormat) {
    if (cNumberFormat.isEmpty() || nNumberFormat.equals(cNumberFormat.get(0))) return false;
    if (nLvl != 0) return false;
    if (cLvl == 0) return true;
    return nNumberFormat.equals(DocumentUnitNumberingListNumberFormat.BULLET);
  }

  private String getListType(
      DocumentUnitNumberingListNumberFormat numberFormat,
      NumberingListEntryIndex numberingListEntryIndex) {
    if (numberingListEntryIndex.lvlPicBullet()) {
      LOGGER.error("Unsupported picture bullet, use default bullet for list");
      return "list-style-type:disc;";
    }
    Pattern docxIndexMatchPattern = Pattern.compile("(%\\d)");
    String lvlText = numberingListEntryIndex.lvlText();
    if (!lvlText.isBlank() && docxIndexMatchPattern.matcher(lvlText).find())
      return "list-style-type:none;";
    return switch (numberFormat) {
      case NONE -> null;
      case DECIMAL -> "list-style-type:decimal;";
      case UPPER_LETTER -> "list-style-type:lower-latin;";
      case LOWER_LETTER -> "list-style-type:upper-latin;";
      case UPPER_ROMAN -> "list-style-type:upper-roman;";
      case LOWER_ROMAN -> "list-style-type:lower-roman";
      case BULLET -> "list-style-type:disc";
      default -> "list-style-type:none;";
    };
  }

  private Integer stringToInt(String value, Integer defaultValue) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      LOGGER.error("Could not convert string value to nummer.", ex);
      return defaultValue;
    }
  }

  private record Index(Integer intIndex, String strIndex) {}
}
