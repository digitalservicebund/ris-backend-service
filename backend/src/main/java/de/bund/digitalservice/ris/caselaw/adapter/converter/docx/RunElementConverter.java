package de.bund.digitalservice.ris.caselaw.adapter.converter.docx;

import static de.bund.digitalservice.ris.caselaw.domain.image.ImageUtil.BASE64_PNG_UNKNOWN_FILE_ICON;
import static de.bund.digitalservice.ris.caselaw.domain.image.ImageUtil.rotateImage;

import de.bund.digitalservice.ris.caselaw.domain.docx.AnchorImageElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.DocxImagePart;
import de.bund.digitalservice.ris.caselaw.domain.docx.ErrorRunElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.InlineImageElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.ParagraphElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.RunElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.RunTabElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.RunTextElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.UnhandledElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.UnhandledElementType;
import de.bund.digitalservice.ris.caselaw.domain.image.ImageRotationAngle;
import de.bund.digitalservice.ris.caselaw.domain.image.ImageUtil;
import jakarta.xml.bind.JAXBElement;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.apache.commons.text.StringEscapeUtils;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.GraphicData;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.CTPosH;
import org.docx4j.dml.wordprocessingDrawing.CTWrapSquare;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.dml.wordprocessingDrawing.STAlignH;
import org.docx4j.dml.wordprocessingDrawing.STWrapText;
import org.docx4j.vml.CTImageData;
import org.docx4j.vml.CTShape;
import org.docx4j.vml.CTShapetype;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.Pict;
import org.docx4j.wml.R;
import org.docx4j.wml.R.LastRenderedPageBreak;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RPrAbstract;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

/**
 * Convert the {@link org.docx4j.wml.R} to a {@link RunElement}
 *
 * @see DocxConverter
 */
public class RunElementConverter {
  private static final Logger LOGGER = LoggerFactory.getLogger(RunElementConverter.class);

  private RunElementConverter() {}

  /**
   * Convert the R element and all its children elements into {@link RunElement}.
   *
   * @param run the start R element
   * @param paragraphElement the parent paragraph element
   * @param converter the used docx converter (contains meta information of the docx file)
   * @param unhandledElements list of unhandled element in the docx file
   */
  public static void convert(
      R run,
      ParagraphElement paragraphElement,
      DocxConverter converter,
      List<UnhandledElement> unhandledElements) {
    run.getContent()
        .forEach(
            element ->
                parseRunChildrenElement(
                    element, run.getRPr(), paragraphElement, converter, unhandledElements));
  }

  private static void parseRunChildrenElement(
      Object element,
      RPr rPr,
      ParagraphElement paragraphElement,
      DocxConverter converter,
      List<UnhandledElement> unhandledElements) {

    if (element instanceof JAXBElement<?> jaxbElement) {
      var declaredType = jaxbElement.getDeclaredType();

      if (declaredType == Text.class) {
        var text = ((Text) jaxbElement.getValue()).getValue();

        if (!text.isEmpty()) {
          paragraphElement.addRunElement(
              generateRunTextElement(text, rPr, paragraphElement, converter));
        }
      } else if (declaredType == Drawing.class) {
        RunElement imageElement =
            parseDrawing(paragraphElement, (Drawing) jaxbElement.getValue(), converter);
        paragraphElement.addRunElement(imageElement);
      } else if (declaredType == R.Tab.class) {
        paragraphElement.addRunElement(new RunTabElement());
      } else if (declaredType == Pict.class) {
        parsePict(paragraphElement, (Pict) jaxbElement.getValue(), converter);
      } else if (declaredType == LastRenderedPageBreak.class) {
        // ignored because in our web presentation of the docx file there are no page breaks
      } else if (declaredType == FldChar.class) {
        // used for field calculations. don't allow calculation at the moment.
      } else if (declaredType == R.NoBreakHyphen.class) {
        RunTextElement runTextElement = new RunTextElement();
        runTextElement.setText("\u2011");
        paragraphElement.addRunElement(runTextElement);
      } else if (declaredType == R.SoftHyphen.class) {
        //  ignored because soft hyphen are represented as constant -
      } else if (declaredType == R.Cr.class) {
        //  carriage return = line break
        RunTextElement runTextElement = new RunTextElement();
        runTextElement.setText("<br/>");
        paragraphElement.addRunElement(runTextElement);
      } else {
        unhandledElements.add(
            new UnhandledElement("run element", declaredType.getName(), UnhandledElementType.JAXB));
        paragraphElement.addRunElement(new ErrorRunElement(declaredType.getName()));
      }
    } else {
      unhandledElements.add(
          new UnhandledElement(
              "run element", element.getClass().getName(), UnhandledElementType.OBJECT));
    }
  }

  private static RunElement generateRunTextElement(
      String text, RPrAbstract rPr, ParagraphElement paragraph, DocxConverter converter) {
    RunTextElement runTextElement = new RunTextElement();
    String htmlEscapedText = StringEscapeUtils.escapeHtml4(text);

    runTextElement.setText(htmlEscapedText);
    addStyle(runTextElement, rPr, paragraph, converter);

    return runTextElement;
  }

  private static RunElement parseDrawing(
      ParagraphElement parent, Drawing drawing, DocxConverter converter) {
    if (drawing.getAnchorOrInline().size() != 1) {
      throw new DocxConverterException("more than one graphic data in a drawing");
    }

    var drawingObject = drawing.getAnchorOrInline().get(0);
    if (drawingObject instanceof Inline inline) {
      return parseInlineImageElement(inline, converter);
    } else if (drawingObject instanceof Anchor anchor) {
      return parseAnchorImageElement(parent, anchor, converter);
    } else {
      LOGGER.error("unsupported drawing object");
      return new ErrorRunElement(
          "anchor drawing object? " + drawingObject.getClass().getSimpleName());
    }
  }

  private static void parsePict(ParagraphElement parent, Pict pict, DocxConverter converter) {
    pict.getAnyAndAny()
        .forEach(
            child -> {
              if (child instanceof JAXBElement<?> jaxbElement) {
                if (jaxbElement.getDeclaredType() == CTShape.class) {
                  parseCTShape(parent, (CTShape) jaxbElement.getValue(), converter);
                } else if (jaxbElement.getValue() instanceof CTShapetype) {
                  LOGGER.warn("Ignore shape type information.");
                } else {
                  LOGGER.error(
                      "unknown jaxb child '{}' in pict element: {}",
                      jaxbElement.getName(),
                      jaxbElement.getValue());
                }
              } else if (child instanceof CTShape shape) {
                parseCTShape(parent, shape, converter);
              } else {
                LOGGER.error("unknown child in pict element: {}", child);
              }
            });
  }

  private static void parseCTShape(
      ParagraphElement parent, CTShape shape, DocxConverter converter) {
    shape
        .getEGShapeElements()
        .forEach(
            jaxbElement -> {
              if (jaxbElement.getValue() instanceof CTImageData imageData) {
                parseCTImageData(parent, imageData, shape.getStyle(), converter);
              } else {
                LOGGER.info(
                    "unknown shape child '{}': {}", jaxbElement.getName(), jaxbElement.getValue());
              }
            });
  }

  private static void parseCTImageData(
      ParagraphElement parent, CTImageData imageData, String style, DocxConverter converter) {
    DocxImagePart image = converter.getImages().get(imageData.getId());
    AnchorImageElement imageElement = new AnchorImageElement();
    imageElement.setContentType(image.contentType());
    var base64 = Base64.getEncoder().encodeToString(image.bytes());
    imageElement.setBase64Representation(base64);
    StyleConverter.getListFromString(style).forEach(imageElement::addStyle);

    parent.addRunElement(imageElement);
  }

  private static RunElement parseAnchorImageElement(
      ParagraphElement parent, Anchor anchor, DocxConverter converter) {
    if (anchor == null
        || anchor.getGraphic() == null
        || anchor.getGraphic().getGraphicData() == null) {
      throw new DocxConverterException("no graphic data");
    }

    Dimension size = parseImageSize(anchor.getExtent());

    RunElement runElement =
        parseGraphicData(
            anchor.getGraphic().getGraphicData(), size, AnchorImageElement.class, converter);

    if (runElement instanceof AnchorImageElement imageElement) {
      imageElement.setAlternateText(parseImageAlternateText(anchor.getDocPr()));
      String floating = parseFloating(anchor);
      if (floating != null) {
        if (floating.equals("error")) {
          if (anchor.getPositionH() == null || anchor.getPositionH().getAlign() == null)
            return new ErrorRunElement("anchor image with unknown alignment: null");
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

  private static RunElement parseInlineImageElement(Inline inline, DocxConverter converter) {
    if (inline == null
        || inline.getGraphic() == null
        || inline.getGraphic().getGraphicData() == null) {
      throw new DocxConverterException("no graphic data");
    }

    Dimension size = parseImageSize(inline.getExtent());

    RunElement runElement =
        parseGraphicData(
            inline.getGraphic().getGraphicData(), size, InlineImageElement.class, converter);

    if (runElement instanceof InlineImageElement imageElement) {
      imageElement.setAlternateText(parseImageAlternateText(inline.getDocPr()));

      return imageElement;
    }

    return runElement;
  }

  private static Dimension parseImageSize(CTPositiveSize2D extent) {
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

  private static String parseImageAlternateText(CTNonVisualDrawingProps docPr) {
    if (docPr == null) {
      return null;
    }

    String alternateText = "";
    alternateText += docPr.getName() != null ? docPr.getName() : "";
    alternateText += docPr.getDescr() != null ? docPr.getDescr() : "";

    return !alternateText.isBlank() ? alternateText : null;
  }

  private static String parseFloating(Anchor anchor) {
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

  private static RunElement parseGraphicData(
      GraphicData graphicData,
      Dimension size,
      Class<? extends InlineImageElement> clazz,
      DocxConverter converter) {

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
      var image = converter.getImages().get(embed);

      if (image != null) {
        if (image.contentType().equals("image/x-emf")) {
          image = new DocxImagePart("image/png", ImageUtil.convertEMF2PNG(image.bytes(), size));
        }

        try {
          Optional<DocxImagePart> optionalRotatedDocxImagePart =
              getRotatedDocxImagePart(image, getImageRotationDegrees(pic.getSpPr()));
          if (optionalRotatedDocxImagePart.isPresent()) {
            image = optionalRotatedDocxImagePart.get();
          }
        } catch (Exception e) {
          LOGGER.error("Error rotating image", e);
        }
        addImageContent(imageElement, image, size);
      }
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

  public static Optional<DocxImagePart> getRotatedDocxImagePart(
      DocxImagePart docxImagePart, ImageRotationAngle rotationAngle) {

    try {
      if (docxImagePart.contentType().equals("image/unknown")) {
        return Optional.empty();
      }

      BufferedImage image =
          rotateImage(ImageIO.read(new ByteArrayInputStream(docxImagePart.bytes())), rotationAngle);

      MediaType format = MediaType.valueOf(docxImagePart.contentType());
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      ImageIO.write(image, format.getSubtype(), byteArrayOutputStream);
      return Optional.of(new DocxImagePart(format.toString(), byteArrayOutputStream.toByteArray()));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private static ImageRotationAngle getImageRotationDegrees(CTShapeProperties ctShapeProperties) {

    if (ctShapeProperties == null || ctShapeProperties.getXfrm() == null) {
      throw new DocxConverterException(
          "Could not extract image rotation, rotation info was not found");
    }
    var rotation = ctShapeProperties.getXfrm().getRot();
    return ImageRotationAngle.fromDegrees(rotation / 60000);
  }

  // convert images before
  // TODO: take out the image input.
  private static void addImageContent(
      InlineImageElement element, DocxImagePart image, Dimension size) {
    element.setContentType(image.contentType());

    if (image.contentType().equals("image/unknown")) {
      element.setContentType("image/png");
      // base64 representation of the material design icon for unknown files
      element.setBase64Representation(BASE64_PNG_UNKNOWN_FILE_ICON);
    } else {
      byte[] bytes = image.bytes();
      element.setBase64Representation(Base64.getEncoder().encodeToString(bytes));
    }
    element.setSize(size);
  }

  private static void addStyle(
      RunTextElement textElement,
      RPrAbstract rPr,
      ParagraphElement paragraph,
      DocxConverter converter) {
    if (rPr != null) {
      RunElementStyleAdapter.addStyles(textElement, rPr);
    } else {
      if (paragraph != null && paragraph.getStyleReference() != null) {
        var style = converter.getStyles().get(paragraph.getStyleReference());

        if (style != null && style.getRPr() != null) {
          RunElementStyleAdapter.addStyles(textElement, style.getRPr());
        }
      }
    }
  }

  public static String parseTextFromRun(R r) {
    return r.getContent().stream()
        .filter(part -> part instanceof JAXBElement<?>)
        .map(part -> (JAXBElement<?>) part)
        .filter(el -> el.getDeclaredType() == Text.class)
        .map(el -> (Text) el.getValue())
        .map(Text::getValue)
        .collect(Collectors.joining());
  }
}
