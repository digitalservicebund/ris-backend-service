package de.bund.digitalservice.ris.utils;

import de.bund.digitalservice.ris.domain.docx.AnchorImageElement;
import de.bund.digitalservice.ris.domain.docx.BorderNumber;
import de.bund.digitalservice.ris.domain.docx.DocumentUnitDocx;
import de.bund.digitalservice.ris.domain.docx.DocxImagePart;
import de.bund.digitalservice.ris.domain.docx.ErrorRunElement;
import de.bund.digitalservice.ris.domain.docx.InlineImageElement;
import de.bund.digitalservice.ris.domain.docx.NumberingList.DocumentUnitNumberingListNumberFormat;
import de.bund.digitalservice.ris.domain.docx.NumberingListEntry;
import de.bund.digitalservice.ris.domain.docx.NumberingListEntryIndex;
import de.bund.digitalservice.ris.domain.docx.ParagraphElement;
import de.bund.digitalservice.ris.domain.docx.RunElement;
import de.bund.digitalservice.ris.domain.docx.RunTabElement;
import de.bund.digitalservice.ris.domain.docx.RunTextElement;
import jakarta.xml.bind.JAXBElement;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.GraphicData;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.CTPosH;
import org.docx4j.dml.wordprocessingDrawing.CTWrapSquare;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.dml.wordprocessingDrawing.STAlignH;
import org.docx4j.dml.wordprocessingDrawing.STWrapText;
import org.docx4j.model.listnumbering.AbstractListNumberingDefinition;
import org.docx4j.model.listnumbering.ListLevel;
import org.docx4j.model.listnumbering.ListNumberingDefinition;
import org.docx4j.vml.CTImageData;
import org.docx4j.vml.CTShape;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.Lvl;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.NumPr;
import org.docx4j.wml.Pict;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RPrAbstract;
import org.docx4j.wml.Style;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentUnitDocxBuilder extends DocxBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentUnitDocxBuilder.class);

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
      return convertToParagraph(paragraph);
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
    if (paragraph == null) {
      return false;
    }

    PPr ppr = paragraph.getPPr();

    if (isText() && ppr != null && ppr.getPStyle() != null) {
      return List.of("RandNummer", "ListParagraph", "Listenabsatz")
          .contains(ppr.getPStyle().getVal());
    }

    return false;
  }

  private BorderNumber convertToBorderNumber() {
    BorderNumber borderNumber = new BorderNumber();

    paragraph.getContent().stream()
        .filter(R.class::isInstance)
        .map(R.class::cast)
        .forEach(r -> borderNumber.addNumberText(parseTextFromRun(r)));

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
    if (!isNumberingListEntry()) {
      return null;
    }

    NumPr numPr = paragraph.getPPr().getNumPr();
    String numId;
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
            DocumentUnitNumberingListNumberFormat.BULLET,
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

    return new NumberingListEntry(convertToParagraph(paragraph), numberingListEntryIndex);
  }

  private NumberingListEntryIndex setNumberingListEntryIndex(ListLevel listLevel, String iLvl) {
    DocumentUnitNumberingListNumberFormat numberFormat;
    switch (listLevel.getNumFmt()) {
      case BULLET -> numberFormat = DocumentUnitNumberingListNumberFormat.BULLET;
      case DECIMAL -> numberFormat = DocumentUnitNumberingListNumberFormat.DECIMAL;
      case UPPER_LETTER -> numberFormat = DocumentUnitNumberingListNumberFormat.UPPER_LETTER;
      case LOWER_LETTER -> numberFormat = DocumentUnitNumberingListNumberFormat.LOWER_LETTER;
      case UPPER_ROMAN -> numberFormat = DocumentUnitNumberingListNumberFormat.UPPER_ROMAN;
      case LOWER_ROMAN -> numberFormat = DocumentUnitNumberingListNumberFormat.LOWER_ROMAN;
      default -> {
        LOGGER.error(
            "not implemented number format ({}) in list. use default bullet list",
            listLevel.getNumFmt());
        numberFormat = DocumentUnitNumberingListNumberFormat.BULLET;
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

  private DocumentUnitDocx convertToParagraph(P paragraph) {
    if (paragraph == null) {
      return null;
    }

    var paragraphElement = new ParagraphElement();

    var pPr = paragraph.getPPr();
    String alignment = getAlignment(pPr);
    if (alignment != null) {
      paragraphElement.setAlignment(alignment);
    }

    paragraph.getContent().stream()
        .filter(R.class::isInstance)
        .map(R.class::cast)
        .forEach(run -> parseRunElement(run, paragraphElement));

    sortParagraphElements(paragraphElement);

    return paragraphElement;
  }

  private void sortParagraphElements(ParagraphElement paragraphElement) {
    List<RunElement> sortedList =
        paragraphElement.getRunElements().stream()
            .sorted(
                (o1, o2) -> {
                  if (o1.getClass().equals(o2.getClass())) {
                    return 0;
                  }

                  if (o1 instanceof AnchorImageElement && !(o2 instanceof AnchorImageElement)) {
                    return -1;
                  }

                  if (!(o1 instanceof AnchorImageElement) && o2 instanceof AnchorImageElement) {
                    return 1;
                  }

                  return 0;
                })
            .toList();

    paragraphElement.setRunElements(sortedList);
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
      } else if (declaredType == Pict.class) {
        parsePict(paragraphElement, (Pict) jaxbElement.getValue());
      } else {
        LOGGER.error("unknown run element: {}", declaredType.getName());
        paragraphElement.addRunElement(new ErrorRunElement(declaredType.getName()));
      }
    }
  }

  private RunElement generateRunTextElement(String text, RPrAbstract rPr) {
    RunTextElement runTextElement = new RunTextElement();

    runTextElement.setText(text);
    addStyle(runTextElement, rPr);

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

  private void parsePict(ParagraphElement parent, Pict pict) {
    pict.getAnyAndAny()
        .forEach(
            child -> {
              if (child instanceof JAXBElement<?> jaxbElement) {
                LOGGER.error(
                    "unknown jaxb child '{}' in pict element: {}",
                    jaxbElement.getName(),
                    jaxbElement.getValue());
              } else if (child instanceof CTShape shape) {
                parseCTShape(parent, shape);
              } else {
                LOGGER.info("unknown child in pict element: {}", child);
              }
            });
  }

  private void parseCTShape(ParagraphElement parent, CTShape shape) {
    shape
        .getEGShapeElements()
        .forEach(
            jaxbElement -> {
              if (jaxbElement.getValue() instanceof CTImageData imageData) {
                parseCTImageData(parent, imageData, shape.getStyle());
              } else {
                LOGGER.info(
                    "unknown shape child '{}': {}", jaxbElement.getName(), jaxbElement.getValue());
              }
            });
  }

  private void parseCTImageData(ParagraphElement parent, CTImageData imageData, String style) {
    DocxImagePart image = images.get(imageData.getId());
    AnchorImageElement imageElement = new AnchorImageElement();
    imageElement.setContentType(image.contentType());
    var base64 = Base64.getEncoder().encodeToString(image.bytes());
    imageElement.setBase64Representation(base64);
    StyleConverter.getListFromString(style).forEach(imageElement::addStyle);
    parent.addRunElement(imageElement);
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
      String floating = parseFloating(anchor);
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

  private String parseFloating(Anchor anchor) {
    CTPosH positionH = anchor.getPositionH();
    CTWrapSquare wrapSquare = anchor.getWrapSquare();

    if ((positionH == null || positionH.getAlign() == null) && (wrapSquare == null)) {
      return null;
    }

    if ((positionH != null && positionH.getAlign() == STAlignH.LEFT)
        || (wrapSquare != null && wrapSquare.getWrapText() == STWrapText.LEFT)) {
      return "left";
    } else if ((positionH != null && positionH.getAlign() == STAlignH.RIGHT)
        || (wrapSquare != null && wrapSquare.getWrapText() == STWrapText.RIGHT)) {
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

  private void addStyle(RunTextElement textElement, RPrAbstract rPr) {
    if (rPr != null) {
      RunElementStyleAdapter.addStyles(textElement, rPr);
    } else {
      if (paragraph != null
          && paragraph.getPPr() != null
          && paragraph.getPPr().getPStyle() != null) {
        var style = styles.get(paragraph.getPPr().getPStyle().getVal());

        if (style != null) {
          if (style.getRPr() != null) {
            RunElementStyleAdapter.addStyles(textElement, style.getRPr());
          }
        }
      }
    }
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
}
