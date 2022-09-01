package de.bund.digitalservice.ris.utils;

import de.bund.digitalservice.ris.domain.docx.*;
import de.bund.digitalservice.ris.domain.docx.NumberingList.DocUnitNumberingListNumberFormat;
import jakarta.xml.bind.JAXBElement;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.GraphicData;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.CTPosH;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.dml.wordprocessingDrawing.STAlignH;
import org.docx4j.model.listnumbering.AbstractListNumberingDefinition;
import org.docx4j.model.listnumbering.ListLevel;
import org.docx4j.model.listnumbering.ListNumberingDefinition;
import org.docx4j.wml.*;
import org.docx4j.wml.PPrBase.NumPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocUnitDocxBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(DocUnitDocxBuilder.class);

  P paragraph;
  Tbl table;
  private Map<String, Style> styles = new HashMap<>();
  private Map<String, DocxImagePart> images = new HashMap<>();
  private Map<String, ListNumberingDefinition> listNumberingDefinitions;

  private DocUnitDocxBuilder() {}

  public static DocUnitDocxBuilder newInstance() {
    return new DocUnitDocxBuilder();
  }

  public DocUnitDocxBuilder useStyles(Map<String, Style> styles) {
    this.styles = styles;

    return this;
  }

  public DocUnitDocxBuilder useImages(Map<String, DocxImagePart> images) {
    this.images = images;

    return this;
  }

  public DocUnitDocxBuilder useListNumberingDefinitions(
      Map<String, ListNumberingDefinition> listNumberingDefinitions) {
    this.listNumberingDefinitions = listNumberingDefinitions;

    return this;
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

    if (isBorderNumber()) {
      return convertToBorderNumber();
    } else if (isNumberingList()) {
      return convertToNumberingList();
    } else if (isParagraph()) {
      return convertToParagraphElement(paragraph);
    }

    return null;
  }

  private boolean isText() {
    if (paragraph == null) {
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
    var isText = isText();

    if (isText && paragraph.getPPr() != null && paragraph.getPPr().getPStyle() != null) {
      return paragraph.getPPr().getPStyle().getVal().equals("RandNummer");
    }

    return false;
  }

  private BorderNumber convertToBorderNumber() {
    BorderNumber borderNumber = new BorderNumber();

    paragraph.getContent().stream()
        .filter(R.class::isInstance)
        .map(R.class::cast)
        .forEach(r -> borderNumber.addNumberText(parseTextFromRun(r)));

    return borderNumber;
  }

  private boolean isNumberingList() {
    if (!isParagraph() || paragraph.getPPr() == null) {
      return false;
    }

    return paragraph.getPPr().getNumPr() != null;
  }

  private NumberingListEntry convertToNumberingList() {
    if (!isNumberingList()) {
      return null;
    }

    NumPr numPr = paragraph.getPPr().getNumPr();
    String numId = null;
    String iLvl = null;

    ListNumberingDefinition listNumberingDefinition = null;
    if (numPr != null && numPr.getNumId() != null && numPr.getNumId().getVal() != null) {
      numId = numPr.getNumId().getVal().toString();
      listNumberingDefinition = listNumberingDefinitions.get(numId);
    }

    if (numPr != null && numPr.getIlvl() != null && numPr.getIlvl().getVal() != null) {
      iLvl = numPr.getIlvl().getVal().toString();
    }

    NumberingListEntryIndex numberingListEntryIndex =
        new NumberingListEntryIndex(
            "",
            "1",
            "",
            "",
            "",
            "",
            false,
            false,
            DocUnitNumberingListNumberFormat.BULLET,
            iLvl,
            JcEnumeration.RIGHT,
            "tab");
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

    return new NumberingListEntry(convertToParagraphElement(paragraph), numberingListEntryIndex);
  }

  private NumberingListEntryIndex setNumberingListEntryIndex(ListLevel listLevel, String iLvl) {
    DocUnitNumberingListNumberFormat numberFormat;
    switch (listLevel.getNumFmt()) {
      case BULLET -> numberFormat = DocUnitNumberingListNumberFormat.BULLET;
      case DECIMAL -> numberFormat = DocUnitNumberingListNumberFormat.DECIMAL;
      case UPPER_LETTER -> numberFormat = DocUnitNumberingListNumberFormat.UPPER_LETTER;
      case LOWER_LETTER -> numberFormat = DocUnitNumberingListNumberFormat.LOWER_LETTER;
      case UPPER_ROMAN -> numberFormat = DocUnitNumberingListNumberFormat.UPPER_ROMAN;
      case LOWER_ROMAN -> numberFormat = DocUnitNumberingListNumberFormat.LOWER_ROMAN;
      default -> {
        LOGGER.error(
            "not implemented number format ({}) in list. use default bullet list",
            listLevel.getNumFmt());
        numberFormat = DocUnitNumberingListNumberFormat.BULLET;
      }
    }
    String restartNummerAfterBreak = "";
    String lvlText = "";
    String suff = "tab";
    String fontColor = "";
    String fontSize = "";
    String fontStyle = "";
    boolean lvlPickBullet = false;
    String startVal = "1";
    JcEnumeration lvlJc = JcEnumeration.RIGHT;
    boolean isLgl = false;

    Lvl lvl = listLevel.getJaxbAbstractLvl();
    if (lvl != null) {
      suff = lvl.getSuff() != null ? lvl.getSuff().getVal() : "tab";
      lvlJc = lvl.getLvlJc() != null ? lvl.getLvlJc().getVal() : JcEnumeration.RIGHT;

      if (listLevel.IsBullet()) {
        lvlPickBullet = lvl.getLvlPicBulletId() != null;
      }
      startVal = lvl.getStart().getVal().toString();
      lvlText = listLevel.getLevelText().isBlank() ? "" : listLevel.getLevelText();
      restartNummerAfterBreak =
          lvl.getLvlRestart() != null ? lvl.getLvlRestart().getVal().toString() : "";
      isLgl = lvl.getIsLgl() != null && lvl.getIsLgl().isVal();

      if (lvl.getRPr() != null) {
        fontColor = lvl.getRPr().getColor() != null ? lvl.getRPr().getColor().getVal() : "";
        fontSize = lvl.getRPr().getSz() != null ? lvl.getRPr().getSz().getVal().toString() : "";
        fontStyle =
            lvl.getRPr().getRFonts() != null && lvl.getRPr().getRFonts().getAscii() != null
                ? lvl.getRPr().getRFonts().getAscii()
                : "";
      }
    }
    return new NumberingListEntryIndex(
        lvlText,
        startVal,
        restartNummerAfterBreak,
        fontColor,
        fontStyle,
        fontSize,
        lvlPickBullet,
        isLgl,
        numberFormat,
        iLvl,
        lvlJc,
        suff);
  }

  private boolean isParagraph() {
    return paragraph != null;
  }

  private DocUnitDocx convertToParagraphElement(P paragraph) {
    if (paragraph == null) {
      return null;
    }

    var paragraphElement = new ParagraphElement();

    var pPr = paragraph.getPPr();
    String alignment = getAlignment(pPr);
    if (alignment != null) {
      paragraphElement.setAlignment(alignment);
    }

    if (!addParagraphStyle(paragraphElement, pPr)) {
      return new ErrorElement("Font size of paragraph is to high.");
    }

    paragraph.getContent().stream()
        .filter(R.class::isInstance)
        .map(R.class::cast)
        .forEach(run -> parseRunElement(run, paragraphElement));

    return paragraphElement;
  }

  private void parseRunElement(R run, ParagraphElement paragraphElement) {
    run.getContent()
        .forEach(element -> parseRunChildrenElement(element, run.getRPr(), paragraphElement));
  }

  private void parseRunChildrenElement(Object element, RPr rPr, ParagraphElement paragraphElement) {
    if (element instanceof JAXBElement<?> jaxbElement) {
      var declaredType = jaxbElement.getDeclaredType();

      if (declaredType == Text.class) {
        var text = ((Text) jaxbElement.getValue()).getValue();

        if (!text.isEmpty()) {
          paragraphElement.addRunElement(generateRunTextElement(text, rPr));
        }
      } else if (declaredType == Drawing.class) {
        RunElement imageElement = parseDrawing(paragraphElement, (Drawing) jaxbElement.getValue());
        paragraphElement.addRunElement(imageElement);
      } else if (declaredType == R.Tab.class) {
        paragraphElement.addRunElement(new RunTabElement());
      } else {
        LOGGER.error("unknown run element: {}", declaredType.getName());
        paragraphElement.addRunElement(new ErrorRunElement(declaredType.getName()));
      }
    }
  }

  private RunElement generateRunTextElement(String text, RPrAbstract rPr) {
    RunTextElement runTextElement = new RunTextElement();

    runTextElement.setText(text);
    if (!addStyle(runTextElement, rPr)) {
      return new ErrorRunElement("Size of the font to high!");
    }

    return runTextElement;
  }

  private RunElement parseDrawing(ParagraphElement parent, Drawing drawing) {
    if (drawing.getAnchorOrInline().size() != 1) {
      throw new DocxConverterException("more than one graphic data in a drawing");
    }

    var drawingObject = drawing.getAnchorOrInline().get(0);
    if (drawingObject instanceof Inline inline) {
      return parseInlineImageElement(inline);
    } else if (drawingObject instanceof Anchor anchor) {
      return parseAnchorImageElement(parent, anchor);
    } else {
      LOGGER.error("unsupported drawing object");
      return new ErrorRunElement(
          "anchor drawing object? " + drawingObject.getClass().getSimpleName());
    }
  }

  private RunElement parseAnchorImageElement(ParagraphElement parent, Anchor anchor) {
    if (anchor == null
        || anchor.getGraphic() == null
        || anchor.getGraphic().getGraphicData() == null) {
      throw new DocxConverterException("no graphic data");
    }

    RunElement runElement =
        parseGraphicData(anchor.getGraphic().getGraphicData(), AnchorImageElement.class);

    if (runElement instanceof AnchorImageElement imageElement) {
      imageElement.setAlternateText(parseImageAlternateText(anchor.getDocPr()));
      imageElement.setSize(parseImageSize(anchor.getExtent()));
      String floating = parseFloating(anchor.getPositionH());
      if (floating != null) {
        if (floating.equals("error")) {
          return new ErrorRunElement(
              "anchor image with unknown alignment: " + anchor.getPositionH().getAlign().value());
        }
        parent.setClearfix(true);
        imageElement.setFloating(floating);
      }

      return imageElement;
    }

    return runElement;
  }

  private RunElement parseInlineImageElement(Inline inline) {
    if (inline == null
        || inline.getGraphic() == null
        || inline.getGraphic().getGraphicData() == null) {
      throw new DocxConverterException("no graphic data");
    }

    RunElement runElement =
        parseGraphicData(inline.getGraphic().getGraphicData(), InlineImageElement.class);

    if (runElement instanceof InlineImageElement imageElement) {
      imageElement.setAlternateText(parseImageAlternateText(inline.getDocPr()));
      imageElement.setSize(parseImageSize(inline.getExtent()));

      return imageElement;
    }

    return runElement;
  }

  private Dimension parseImageSize(CTPositiveSize2D extent) {
    if (extent == null) {
      return null;
    }

    Dimension size = new Dimension();
    if (extent.getCx() != 0) {
      size.width = DocxUnitConverter.convertEMUToPixel(extent.getCx());
    }

    if (extent.getCy() != 0) {
      size.height = DocxUnitConverter.convertEMUToPixel(extent.getCy());
    }

    return size;
  }

  private String parseImageAlternateText(CTNonVisualDrawingProps docPr) {
    if (docPr == null) {
      return null;
    }

    String alternateText = "";
    alternateText += docPr.getName() != null ? docPr.getName() : "";
    alternateText += docPr.getDescr() != null ? docPr.getDescr() : "";

    return !alternateText.isBlank() ? alternateText : null;
  }

  private String parseFloating(CTPosH positionH) {
    if (positionH == null || positionH.getAlign() == null) {
      return null;
    }

    if (positionH.getAlign() == STAlignH.LEFT) {
      return "left";
    } else if (positionH.getAlign() == STAlignH.RIGHT) {
      return "right";
    } else {
      return "error";
    }
  }

  private RunElement parseGraphicData(
      GraphicData graphicData, Class<? extends InlineImageElement> clazz) {

    InlineImageElement imageElement = new InlineImageElement();
    try {
      imageElement = clazz.getDeclaredConstructor().newInstance();
    } catch (InstantiationException
        | NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException e) {
      LOGGER.error("Couldn't instantiate image class: {}", clazz.getSimpleName(), e);
    }

    var pic = graphicData.getPic();

    if (pic != null) {
      var embed = pic.getBlipFill().getBlip().getEmbed();
      var image = images.get(embed);

      var base64 = Base64.getEncoder().encodeToString(image.bytes());
      imageElement.setBase64Representation(base64);
      imageElement.setContentType(image.contentType());
    } else {
      LOGGER.error("no picture");
      List<Object> anyGraphicElement = graphicData.getAny();
      StringBuilder stringBuilder = new StringBuilder();
      anyGraphicElement.forEach(
          el -> stringBuilder.append(el.getClass().getSimpleName()).append(", "));
      return new ErrorRunElement("unknown graphic element: " + stringBuilder);
    }

    return imageElement;
  }

  private String getAlignment(PPr pPr) {
    if (pPr == null) {
      return null;
    }

    Jc jc = null;

    var pStyle = pPr.getPStyle();
    if (pStyle != null && pStyle.getVal() != null) {
      Style style = styles.get(pStyle.getVal());
      if (style != null && style.getPPr() != null) {
        jc = style.getPPr().getJc();
      }
    }

    if (pPr.getJc() != null) {
      jc = pPr.getJc();
    }

    if (jc != null && jc.getVal() != null && jc.getVal() == JcEnumeration.CENTER) {
      return "center";
    }

    if (jc != null && jc.getVal() != null && jc.getVal() == JcEnumeration.RIGHT) {
      return "right";
    }

    if (jc != null && jc.getVal() != null && jc.getVal() == JcEnumeration.LEFT) {
      return "left";
    }

    if (jc != null && jc.getVal() != null && jc.getVal() == JcEnumeration.BOTH) {
      return "justify";
    }

    return null;
  }

  private boolean addParagraphStyle(TextElement textElement, PPr pPr) {
    if (pPr == null) {
      return true;
    }

    RPrAbstract styleRPr = null;
    var pStyle = pPr.getPStyle();
    if (pStyle != null && pStyle.getVal() != null) {
      var style = styles.get(pStyle.getVal());
      if (style != null) {
        styleRPr = style.getRPr();
      }
    }

    textElement.setBold(isBold(styleRPr, pPr.getRPr()));
    textElement.setItalic(isItalic(styleRPr, pPr.getRPr()));
    textElement.setStrike(isStrike(styleRPr, pPr.getRPr()));
    textElement.setVertAlign(getVertAlign(styleRPr, pPr.getRPr()));

    var size = getSize(styleRPr, pPr.getRPr());
    if (size != null) {
      if (size.compareTo(new BigInteger(String.valueOf(Integer.MAX_VALUE))) > 0) {
        return false;
      }
      textElement.setSize(size.intValue());
    }

    var underline = getUnderline(styleRPr, pPr.getRPr());
    if (underline != null) {
      textElement.setUnderline(underline);
    }

    return true;
  }

  private boolean isBold(RPrAbstract styleRPr, RPrAbstract rPr) {
    if (styleRPr == null && rPr == null) {
      return false;
    }

    boolean bold = false;
    if (styleRPr != null && styleRPr.getB() != null) {
      bold = styleRPr.getB().isVal();
    }

    if (rPr != null && rPr.getB() != null && rPr.getB().isVal()) {
      bold = rPr.getB().isVal();
    }

    return bold;
  }

  private boolean isItalic(RPrAbstract styleRPr, RPrAbstract rPr) {
    if (styleRPr == null && rPr == null) {
      return false;
    }

    boolean italic = false;
    if (styleRPr != null && styleRPr.getI() != null) {
      italic = styleRPr.getI().isVal();
    }

    if (rPr != null && rPr.getI() != null && rPr.getI().isVal()) {
      italic = rPr.getI().isVal();
    }

    return italic;
  }

  private VerticalAlign getVertAlign(RPrAbstract styleRPr, RPrAbstract rPr) {

    if (styleRPr == null && rPr == null) {
      return null;
    }

    STVerticalAlignRun vertAlign = null;
    if (styleRPr != null
        && styleRPr.getVertAlign() != null
        && styleRPr.getVertAlign().getVal() != null) {
      vertAlign = styleRPr.getVertAlign().getVal();
    }

    if (rPr != null && rPr.getVertAlign() != null && rPr.getVertAlign().getVal() != null) {
      vertAlign = rPr.getVertAlign().getVal();
    }

    if (vertAlign != null && vertAlign != STVerticalAlignRun.BASELINE) {
      if (vertAlign == STVerticalAlignRun.SUBSCRIPT) {
        return VerticalAlign.SUBSCRIPT;
      } else if (vertAlign == STVerticalAlignRun.SUPERSCRIPT) {
        return VerticalAlign.SUPERSCRIPT;
      } else {
        LOGGER.error("Unknown vertical align value: {}", vertAlign);
      }
    }
    return null;
  }

  private BigInteger getSize(RPrAbstract styleRPr, RPrAbstract rPr) {
    if (styleRPr == null && rPr == null) {
      return null;
    }

    BigInteger size = null;
    if (styleRPr != null && styleRPr.getSz() != null && styleRPr.getSz().getVal() != null) {
      size = styleRPr.getSz().getVal();
    }

    if (rPr != null && rPr.getSz() != null && rPr.getSz().getVal() != null) {
      size = rPr.getSz().getVal();
    }

    return size;
  }

  private String getUnderline(RPrAbstract styleRPr, RPrAbstract rPr) {
    if (styleRPr == null && rPr == null) {
      return null;
    }

    UnderlineEnumeration underline = null;
    if (styleRPr != null && styleRPr.getU() != null && styleRPr.getU().getVal() != null) {
      underline = styleRPr.getU().getVal();
    }

    if (rPr != null && rPr.getU() != null && rPr.getU().getVal() != null) {
      underline = rPr.getU().getVal();
    }

    if (underline == UnderlineEnumeration.SINGLE) {
      return "single";
    }

    return null;
  }

  private boolean isStrike(RPrAbstract styleRPr, RPrAbstract rPr) {
    if (styleRPr == null && rPr == null) {
      return false;
    }

    boolean strike = false;
    if (styleRPr != null && styleRPr.getStrike() != null) {
      strike = styleRPr.getStrike().isVal();
    }

    if (rPr != null && rPr.getStrike() != null) {
      strike = rPr.getStrike().isVal();
    }

    return strike;
  }

  private boolean addStyle(TextElement textElement, RPrAbstract rPr) {
    if (rPr == null) {
      return true;
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

    if (rPr.getVertAlign() != null) {
      STVerticalAlignRun vertAlign = rPr.getVertAlign().getVal();
      VerticalAlign convertedVertAlign = null;
      if (vertAlign != null && vertAlign != STVerticalAlignRun.BASELINE) {
        if (vertAlign == STVerticalAlignRun.SUBSCRIPT) {
          convertedVertAlign = VerticalAlign.SUBSCRIPT;
        } else if (vertAlign == STVerticalAlignRun.SUPERSCRIPT) {
          convertedVertAlign = VerticalAlign.SUPERSCRIPT;
        } else {
          LOGGER.error("Unknown vertical align value: {}", vertAlign);
        }
      }

      textElement.setVertAlign(convertedVertAlign);
    }

    if (rPr.getSz() != null) {
      if (rPr.getSz().getVal().compareTo(new BigInteger(String.valueOf(Integer.MAX_VALUE))) > 0) {
        return false;
      }
      textElement.setSize(rPr.getSz().getVal().intValue());
    }

    if (rPr.getU() != null && rPr.getU().getVal() == UnderlineEnumeration.SINGLE) {
      textElement.setUnderline("single");
    }

    return true;
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

  private void addTableStyle(TableElement tableElement, TblPr tblPr) {
    if (tblPr == null) return;

    if (tblPr.getTblBorders() != null) {
      var topBorder = tblPr.getTblBorders().getTop();
      tableElement.setTopBorder(parseCtBorder(topBorder));

      var rightBorder = tblPr.getTblBorders().getRight();
      tableElement.setRightBorder(parseCtBorder(rightBorder));

      var bottomBorder = tblPr.getTblBorders().getBottom();
      tableElement.setBottomBorder(parseCtBorder(bottomBorder));

      var leftBorder = tblPr.getTblBorders().getLeft();
      tableElement.setLeftBorder(parseCtBorder(leftBorder));
    }

    if (tblPr.getShd() != null) tableElement.setBackgroundColor(parseCTShd(tblPr.getShd()));
  }

  private Border parseCtBorder(CTBorder border) {
    if (border == null) return null;

    var color = border.getColor();
    if (color != null) {
      color = color.equals("auto") ? "000" : color;
      color = "#" + color.toLowerCase();
    }

    var width =
        border.getSz() != null ? DocxUnitConverter.convertPointToPixel(border.getSz()) : null;

    if (!border.getVal().equals(STBorder.SINGLE)) LOGGER.error("unsupported table border style");
    var type = "solid";

    return new Border(color, width, type);
  }

  private String parseCTShd(CTShd ctShd) {
    if (!ctShd.getVal().equals(STShd.CLEAR)) LOGGER.error("unsupported shading value (STShd)");
    return "#" + ctShd.getFill();
  }

  private DocUnitDocx convertToTable() {
    var tableElement = new TableElement(parseTable(table));
    addTableStyle(tableElement, table.getTblPr());

    return tableElement;
  }

  private List<TableRowElement> parseTable(Tbl table) {
    List<TableRowElement> rows = new ArrayList<>();

    table
        .getContent()
        .forEach(
            element -> {
              if (element instanceof Tr tr) {
                rows.add(parseTr(tr));
              } else {
                LOGGER.error("unknown table element: {}", element.getClass());
              }
            });

    if (!rows.isEmpty()) {
      rows.get(0).cells.forEach(BlockElement::removeTopBorder);
      rows.get(rows.size() - 1).cells.forEach(BlockElement::removeBottomBorder);
    }

    return rows;
  }

  private TableRowElement parseTr(Tr tr) {
    List<TableCellElement> cells = new ArrayList<>();

    tr.getContent()
        .forEach(
            element -> {
              if (element instanceof JAXBElement<?> jaxbElement) {
                if (jaxbElement.getDeclaredType() == Tc.class) {
                  cells.add(parseTc((Tc) jaxbElement.getValue()));
                } else {
                  LOGGER.error("unknown tr element: {}", jaxbElement.getDeclaredType());
                }
              } else {
                LOGGER.error("unknown tr element: {}", element.getClass());
              }
            });

    if (!cells.isEmpty() && table.getTblPr().getTblBorders() != null) {
      var verticalCtBorder = table.getTblPr().getTblBorders().getInsideV();
      var horizontalCtBorder = table.getTblPr().getTblBorders().getInsideH();

      var verticalBorder = parseCtBorder(verticalCtBorder);
      var horizontalBorder = parseCtBorder(horizontalCtBorder);

      cells.forEach(
          cell -> {
            cell.setTopBorder(horizontalBorder);
            cell.setRightBorder(verticalBorder);
            cell.setBottomBorder(horizontalBorder);
            cell.setLeftBorder(verticalBorder);
          });
      cells.get(0).setLeftBorder(null);
      cells.get(cells.size() - 1).setRightBorder(null);
    }

    return new TableRowElement(cells);
  }

  private TableCellElement parseTc(Tc tc) {
    List<DocUnitDocx> paragraphElements = new ArrayList<>();
    tc.getContent()
        .forEach(
            element -> {
              if (element instanceof P p) {
                paragraphElements.add(convertToParagraphElement(p));
              } else {
                LOGGER.error("unknown tr element");
              }
            });

    var cell = new TableCellElement(paragraphElements);

    var tcPr = tc.getTcPr();
    if (tcPr != null) {
      if (tcPr.getTcBorders() != null) {
        var tcBorders = tcPr.getTcBorders();
        cell.setInitialBorders(
            parseCtBorder(tcBorders.getTop()),
            parseCtBorder(tcBorders.getRight()),
            parseCtBorder(tcBorders.getBottom()),
            parseCtBorder(tcBorders.getLeft()));
      }
      if (tcPr.getGridSpan() != null) cell.setColumnSpan(tcPr.getGridSpan().getVal().intValue());
      if (tcPr.getShd() != null) cell.setBackgroundColor(parseCTShd(tcPr.getShd()));
    }

    return cell;
  }
}
