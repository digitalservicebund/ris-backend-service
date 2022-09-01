package de.bund.digitalservice.ris.domain.docx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumberingList implements DocUnitDocx {

  private final List<NumberingListEntry> entries = new ArrayList<>();
  private static final Logger LOGGER = LoggerFactory.getLogger(NumberingList.class);

  public NumberingList() {
    /*Create new instance of docUnitNumberList*/
  }

  public void addNumberingListEntry(NumberingListEntry entry) {
    entries.add(entry);
  }

  @Override
  public String toHtmlString() {
    StringBuilder sb = new StringBuilder();
    LinkedList<String> closeTags = new LinkedList<>();
    int[] cLvl = {-1};
    List<DocUnitNumberingListNumberFormat> currentNumberFormat = new ArrayList<>();
    HashMap<Integer, Index> lvlTextIndex = new HashMap<>();

    entries.forEach(
        entry -> {
          boolean resetLvlText = false;
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
            resetLvlText = true;
          }

          /* Open list/sub-list Tag*/
          while (lvl > cLvl[0]) {
            sb.append(getOpenListTag(entry.numberingListEntryIndex()));
            closeTags.addFirst(getCloseListTag(entry.numberingListEntryIndex().numberFormat()));
            cLvl[0]++;
            resetLvlText = true;
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
            String strIndex =
                getIndexWithFormat(
                    lvlTextIndex, resetLvlText, cLvl[0], entry.numberingListEntryIndex());
            sb.append("<li style=\"display:table-row\">")
                .append(strIndex)
                .append(entry.toHtmlString())
                .append("</li>");
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

  public enum DocUnitNumberingListNumberFormat {
    DECIMAL,
    BULLET,
    UPPER_ROMAN,
    LOWER_ROMAN,
    UPPER_LETTER,
    LOWER_LETTER,
  }

  private String getOpenListTag(NumberingListEntryIndex numberingListEntryIndex) {
    DocUnitNumberingListNumberFormat listNumberFormat = numberingListEntryIndex.numberFormat();
    String listStyle = getListType(listNumberFormat, numberingListEntryIndex);
    final String numOpenTagFormat = "<ol style=\"%s\">";
    final String bulletOpenTagFormat = "<ul style=\"%s\">";
    return switch (listNumberFormat) {
      case BULLET -> String.format(bulletOpenTagFormat, listStyle);
      default -> String.format(numOpenTagFormat, listStyle);
    };
  }

  private String getCloseListTag(DocUnitNumberingListNumberFormat listNumberFormat) {
    return listNumberFormat == DocUnitNumberingListNumberFormat.BULLET ? "</ul>" : "</ol>";
  }

  private boolean shouldCreateNewList(
      int cLvl,
      int nLvl,
      List<DocUnitNumberingListNumberFormat> cNumberFormat,
      DocUnitNumberingListNumberFormat nNumberFormat) {
    if (cNumberFormat.isEmpty() || nNumberFormat.equals(cNumberFormat.get(0))) return false;
    if (nLvl != 0) return false;
    if (cLvl == 0) return true;
    return nNumberFormat.equals(DocUnitNumberingListNumberFormat.BULLET);
  }

  private String getPStyle(NumberingListEntryIndex numberingListEntryIndex) {
    StringBuilder sb = new StringBuilder();
    if (!numberingListEntryIndex.fontStyle().isBlank())
      sb.append("font-family:").append(numberingListEntryIndex.fontStyle()).append(";");
    if (!numberingListEntryIndex.fontSize().isBlank()) {
      Integer fontSize = stringToInt(numberingListEntryIndex.fontSize(), 0);
      if (fontSize > 0) {
        sb.append("font-size:").append(fontSize / 2).append("pt").append(";");
      }
    }
    if (!numberingListEntryIndex.color().isBlank())
      sb.append("color:").append("#").append(numberingListEntryIndex.color()).append(";");
    return sb.toString().isBlank() ? "" : sb.toString();
  }

  private String getListType(
      DocUnitNumberingListNumberFormat numberFormat,
      NumberingListEntryIndex numberingListEntryIndex) {
    if (numberingListEntryIndex.lvlPicBullet()) {
      LOGGER.error("Unsupported picture bullet, use default bullet for list");
      return "list-style-type:disc;";
    }
    boolean hasStyle =
        (!numberingListEntryIndex.fontStyle().isBlank())
            || (!numberingListEntryIndex.color().isBlank())
            || (!numberingListEntryIndex.fontSize().isBlank());
    if (hasStyle) return "list-style-type:none;display:table;";
    Pattern docxIndexMatchPattern = Pattern.compile("(%\\d)");
    String lvlText = numberingListEntryIndex.lvlText();
    if (!lvlText.isBlank() && docxIndexMatchPattern.matcher(lvlText).find())
      return "list-style-type:none;display:table;";
    return switch (numberFormat) {
      case DECIMAL -> "list-style-type:decimal;";
      case UPPER_LETTER -> "list-style-type:lower-latin;";
      case LOWER_LETTER -> "list-style-type:upper-latin;";
      case UPPER_ROMAN -> "list-style-type:upper-roman;";
      case LOWER_ROMAN -> "list-style-type:lower-roman";
      default -> "list-style-type:disc;";
    };
  }

  private String intToRoman(int number) {
    String[] thousands = {"", "M", "MM", "MMM"};
    String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
    String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
    String[] units = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
    return new StringBuilder()
        .append(thousands[number / 1000])
        .append(hundreds[(number % 1000) / 100])
        .append(tens[(number % 100) / 10])
        .append(units[number % 10])
        .toString();
  }

  private String intToLatin(int number) {
    String[] alphabet = {
      "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
      "T", "U", "V", "W", "X", "Y", "Z", "Ä", "Ö", "Ü", "ß"
    };
    return alphabet[number % alphabet.length];
  }

  private String getBulletHexCode(String rFont) {
    if (rFont == null || rFont.isBlank()) return "&#9679;";
    return switch (rFont) {
      case "Courier New" -> "&#9675;";
      case "Wingdings" -> "&#9642;";
      default -> "&#9679;";
    };
  }

  private String getNumericIndex(
      int number, DocUnitNumberingListNumberFormat docUnitNumberingListNumberFormat) {
    return switch (docUnitNumberingListNumberFormat) {
      case UPPER_LETTER -> intToLatin(number).toUpperCase();
      case LOWER_LETTER -> intToLatin(number).toLowerCase();
      case UPPER_ROMAN -> intToRoman(number + 1).toUpperCase();
      case LOWER_ROMAN -> intToRoman(number + 1).toLowerCase();
      default -> String.valueOf(number + 1);
    };
  }

  private String getIndexJc(NumberingListEntryIndex numberingListEntryIndex) {
    StringBuilder sb = new StringBuilder().append("display:table-cell;");
    sb.append(
        switch (numberingListEntryIndex.lvlJc()) {
          case LEFT -> "text-align:left;";
          case CENTER -> "text-align:center;";
          default -> "text-align:right;";
        });
    return sb.toString();
  }

  private String getSuff(NumberingListEntryIndex numberingListEntryIndex) {
    return switch (numberingListEntryIndex.suff()) {
      case "nothing" -> "";
      case "space" -> " ";
      default -> "&emsp;";
    };
  }

  private String getIndexWithFormat(
      HashMap<Integer, Index> hashMapTextLvl,
      boolean resetLvlText,
      int currentLvl,
      NumberingListEntryIndex numberingListEntryIndex) {
    DocUnitNumberingListNumberFormat numberFormat = numberingListEntryIndex.numberFormat();
    final String listIndexFormat =
        "<p style=\"%s\"><span style=\"%s\">%s</span><span>%s</span></p>";
    String indexJc = getIndexJc(numberingListEntryIndex);
    String pStyle = getPStyle(numberingListEntryIndex);
    String suff = getSuff(numberingListEntryIndex);
    if (numberFormat.equals(DocUnitNumberingListNumberFormat.BULLET)) {
      String bulletHexCode = getBulletHexCode(numberingListEntryIndex.fontStyle());
      return String.format(listIndexFormat, indexJc, pStyle, bulletHexCode, suff);
    }
    int index = stringToInt(numberingListEntryIndex.startVal(), 1) - 1;
    String strIndex = numberingListEntryIndex.lvlText();

    if (hashMapTextLvl.containsKey(currentLvl)) {
      index = hashMapTextLvl.get(currentLvl).intIndex;
      if (!resetLvlText) {
        index += 1;
      }
      if (!numberingListEntryIndex.restartNumberingAfterBreak().isBlank()) {
        int step = stringToInt(numberingListEntryIndex.restartNumberingAfterBreak(), 0);
        index += step;
      }
    }
    hashMapTextLvl.put(currentLvl, new Index(index, getNumericIndex(index, numberFormat)));

    while (currentLvl >= 0) {
      String pattern = new StringBuilder().append("%").append(currentLvl + 1).toString();
      if (strIndex.contains(pattern)) {
        strIndex =
            strIndex.replace(
                pattern,
                hashMapTextLvl.containsKey(currentLvl)
                    ? hashMapTextLvl.get(currentLvl).strIndex()
                    : "");
      }
      currentLvl--;
    }
    return String.format(listIndexFormat, indexJc, pStyle, strIndex, suff);
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
