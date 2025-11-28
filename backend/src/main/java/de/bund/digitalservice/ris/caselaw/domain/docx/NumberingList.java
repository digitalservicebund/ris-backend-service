package de.bund.digitalservice.ris.caselaw.domain.docx;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumberingList implements DocumentationUnitDocx {

  // this should be a list containing either NumberingListEntry's or NumberingList's
  // so that a tree structure that can recursively be traversed
  private final List<NumberingListEntry> entries = new ArrayList<>();
  private static final Logger LOGGER = LoggerFactory.getLogger(NumberingList.class);

  public NumberingList() {
    /*Create new instance of DocumentationUnitNumberList*/
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
    final boolean[] isListItemOpen = {false};
    List<DocumentationUnitNumberingListNumberFormat> currentNumberFormat = new ArrayList<>();

    entries.forEach(
        entry -> {
          /*Get level of list entry*/
          int lvl = stringToInt(entry.numberingListEntryIndex().iLvl(), 0);

          // Open a new list if change in number format at level 0
          if (shouldCreateNewList(
              cLvl[0], lvl, currentNumberFormat, entry.numberingListEntryIndex().numberFormat())) {
            // Check if the last list item is still open
            // because for sub-lists the last list item
            // should remain open.
            if (isListItemOpen[0]) {
              sb.append("</li>");
              isListItemOpen[0] = false;
            }

            while (!closeTags.isEmpty()) {
              sb.append(closeTags.removeFirst());
              // If closing sub list, also close parent list
              if (!closeTags.isEmpty()) {
                sb.append("</li>");
              }
            }

            sb.append(getOpenListTag(entry.numberingListEntryIndex()));
            closeTags.addFirst(getCloseListTag(entry.numberingListEntryIndex().numberFormat()));
            cLvl[0] = lvl;
          }

          // Close list/sub-list Tag
          while (lvl < cLvl[0]) {
            // Close item
            if (isListItemOpen[0]) {
              sb.append("</li>");
              isListItemOpen[0] = false;
            }

            // Close sub list
            sb.append(closeTags.removeFirst());

            // Closing parent list item that contained this sub list
            sb.append("</li>");

            cLvl[0]--;
          }

          // If still open, then close it
          if (lvl == cLvl[0] && isListItemOpen[0]) {
            sb.append("</li>");
            isListItemOpen[0] = false;
          }

          // Open list/sub-list Tag
          while (lvl > cLvl[0]) {
            sb.append(getOpenListTag(entry.numberingListEntryIndex()));
            closeTags.addFirst(getCloseListTag(entry.numberingListEntryIndex().numberFormat()));
            cLvl[0]++;
          }

          if (entry.numberingListEntryIndex().isLgl()) {
            sb.append("<li style=\"list-style-type:decimal\">");
          } else {
            sb.append("<li>");
          }

          sb.append(entry.toHtmlString());

          // Leaving list open for later closing.
          isListItemOpen[0] = true;

          if (!currentNumberFormat.isEmpty()) {
            currentNumberFormat.remove(0);
          }
          currentNumberFormat.add(entry.numberingListEntryIndex().numberFormat());
          cLvl[0] = lvl;
        });

    // Final list close
    if (isListItemOpen[0]) {
      sb.append("</li>");
    }

    /* Close all list/sub-list tag when last element*/
    while (!closeTags.isEmpty()) {
      sb.append(closeTags.removeFirst());

      // Closing parent list item that contained this sub list
      if (!closeTags.isEmpty()) {
        sb.append("</li>");
      }
    }
    return sb.toString();
  }

  public enum DocumentationUnitNumberingListNumberFormat {
    DECIMAL,
    BULLET,
    UPPER_ROMAN,
    LOWER_ROMAN,
    UPPER_LETTER,
    LOWER_LETTER,
  }

  private String getOpenListTag(NumberingListEntryIndex numberingListEntryIndex) {
    DocumentationUnitNumberingListNumberFormat listNumberFormat =
        numberingListEntryIndex.numberFormat();
    String listStyle = getListType(listNumberFormat, numberingListEntryIndex);
    if (listNumberFormat == DocumentationUnitNumberingListNumberFormat.BULLET) {
      return String.format("<ul style=\"%s\">", listStyle);
    } else {
      return String.format("<ol style=\"%s\">", listStyle);
    }
  }

  private String getCloseListTag(DocumentationUnitNumberingListNumberFormat listNumberFormat) {
    return listNumberFormat == DocumentationUnitNumberingListNumberFormat.BULLET
        ? "</ul>"
        : "</ol>";
  }

  private boolean shouldCreateNewList(
      int cLvl,
      int nLvl,
      List<DocumentationUnitNumberingListNumberFormat> cNumberFormat,
      DocumentationUnitNumberingListNumberFormat nNumberFormat) {
    if (cNumberFormat.isEmpty() || nNumberFormat.equals(cNumberFormat.get(0))) return false;
    if (nLvl != 0) return false;
    if (cLvl == 0) return true;
    return nNumberFormat.equals(DocumentationUnitNumberingListNumberFormat.BULLET);
  }

  private String getListType(
      DocumentationUnitNumberingListNumberFormat numberFormat,
      NumberingListEntryIndex numberingListEntryIndex) {
    if (numberingListEntryIndex.lvlPicBullet()) {
      LOGGER.error("Unsupported picture bullet, use default bullet for list");
      return "list-style-type:disc;";
    }
    return switch (numberFormat) {
      case DECIMAL -> "list-style-type:decimal;";
      case UPPER_LETTER -> "list-style-type:upper-latin;";
      case LOWER_LETTER -> "list-style-type:lower-latin;";
      case UPPER_ROMAN -> "list-style-type:upper-roman;";
      case LOWER_ROMAN -> "list-style-type:lower-roman;";
      case BULLET -> "list-style-type:disc;";
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
