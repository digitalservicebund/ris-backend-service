package de.bund.digitalservice.ris.caselaw.domain.docx;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumberingList implements DocumentationUnitDocx {

  public static final String CLOSING_LIST_ITEM = "</li>";
  public static final String OPENING_LIST_ITEM = "<li>";
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
    int[] currentLevel = {-1};
    final boolean[] isListItemOpen = {false};
    List<DocumentationUnitNumberingListNumberFormat> currentNumberFormat = new ArrayList<>();

    entries.forEach(
        entry -> {
          /*Get level of list entry*/
          int targetLevel = stringToInt(entry.numberingListEntryIndex().iLvl(), 0);

          // Open a new list if change in number format at level 0
          if (shouldCreateNewList(
              currentLevel[0],
              targetLevel,
              currentNumberFormat,
              entry.numberingListEntryIndex().numberFormat())) {

            // Check if the last list item is still open
            // because for sub-lists the last list item
            // should remain open.
            if (isListItemOpen[0]) {
              sb.append(CLOSING_LIST_ITEM);
              isListItemOpen[0] = false;
            }

            handleListClosing(closeTags, sb);

            sb.append(getOpenListTag(entry.numberingListEntryIndex()));
            closeTags.addFirst(getCloseListTag(entry.numberingListEntryIndex().numberFormat()));
            currentLevel[0] = targetLevel;
          }

          closeSubList(targetLevel, closeTags, sb, currentLevel, isListItemOpen);

          // If still open, then close it
          if (targetLevel == currentLevel[0] && isListItemOpen[0]) {
            sb.append(CLOSING_LIST_ITEM);
            isListItemOpen[0] = false;
          }

          // Open list/sub-list Tag
          while (targetLevel > currentLevel[0]) {
            sb.append(getOpenListTag(entry.numberingListEntryIndex()));
            closeTags.addFirst(getCloseListTag(entry.numberingListEntryIndex().numberFormat()));
            currentLevel[0]++;
          }

          openListItemWithStyle(entry, sb);

          sb.append(entry.toHtmlString());

          // Leaving list open for later closing.
          isListItemOpen[0] = true;

          if (!currentNumberFormat.isEmpty()) {
            currentNumberFormat.removeFirst();
          }
          currentNumberFormat.add(entry.numberingListEntryIndex().numberFormat());
          currentLevel[0] = targetLevel;
        });

    // Final list close
    if (isListItemOpen[0]) {
      sb.append(CLOSING_LIST_ITEM);
    }

    closeAllLists(closeTags, sb);

    return sb.toString();
  }

  private void closeSubList(
      int targetLevel,
      LinkedList<String> closeTags,
      StringBuilder sb,
      int[] currentLevel,
      boolean[] isListItemOpen) {
    while (targetLevel < currentLevel[0]) {
      // Close item
      if (isListItemOpen[0]) {
        sb.append(CLOSING_LIST_ITEM);
        isListItemOpen[0] = false;
      }

      // Close sub list
      sb.append(closeTags.removeFirst());

      // Closing parent list item that contained this sub list
      sb.append(CLOSING_LIST_ITEM);

      currentLevel[0]--;
    }
  }

  private void handleListClosing(LinkedList<String> closeTags, StringBuilder sb) {
    while (!closeTags.isEmpty()) {
      sb.append(closeTags.removeFirst());
      // If closing sub list, also close parent list
      if (!closeTags.isEmpty()) {
        sb.append(CLOSING_LIST_ITEM);
      }
    }
  }

  private void openListItemWithStyle(NumberingListEntry entry, StringBuilder sb) {
    if (entry.numberingListEntryIndex().isLgl()) {
      sb.append("<li style=\"list-style-type:decimal\">");
    } else {
      sb.append(OPENING_LIST_ITEM);
    }
  }

  private void closeAllLists(LinkedList<String> closeTags, StringBuilder sb) {
    while (!closeTags.isEmpty()) {
      sb.append(closeTags.removeFirst());

      // Closing parent list item that contained this sub list
      if (!closeTags.isEmpty()) {
        sb.append(CLOSING_LIST_ITEM);
      }
    }
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
