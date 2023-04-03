package de.bund.digitalservice.ris.caselaw.adapter.converter.docx;

import de.bund.digitalservice.ris.caselaw.domain.docx.DocxImagePart;
import jakarta.xml.bind.JAXBElement;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.docx4j.dml.CTBlip;
import org.docx4j.dml.CTBlipFillProperties;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.GraphicData;
import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.vml.CTImageData;
import org.docx4j.vml.CTShape;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.Pict;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;

public class TestDocxBuilder {

  public static ParagraphBuilder newParagraphBuilder() {
    return new ParagraphBuilder();
  }

  public static R buildTextRunElement(String text) {
    R runElement = new R();
    Text textElement = new Text();
    textElement.setValue(text);
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, textElement);
    runElement.getContent().add(element);
    return runElement;
  }

  public static R buildAnchorImageElement() {
    R runElement = new R();
    Drawing drawing = new Drawing();

    Anchor anchor = new Anchor();
    Graphic graphic = new Graphic();
    GraphicData graphicData = new GraphicData();
    Pic pic = new Pic();
    CTBlipFillProperties blibFill = new CTBlipFillProperties();
    CTBlip blib = new CTBlip();
    blib.setEmbed("anchor-ref");
    blibFill.setBlip(blib);
    pic.setBlipFill(blibFill);
    graphicData.getAny().add(pic);
    graphic.setGraphicData(graphicData);
    anchor.setGraphic(graphic);

    drawing.getAnchorOrInline().add(anchor);

    JAXBElement<Drawing> element = new JAXBElement<>(new QName("drawing"), Drawing.class, drawing);
    runElement.getContent().add(element);
    return runElement;
  }

  public static R buildInlineImageElement() {
    R runElement = new R();
    Drawing drawing = new Drawing();

    Inline inline = new Inline();
    Graphic graphic = new Graphic();
    GraphicData graphicData = new GraphicData();
    Pic pic = new Pic();
    CTBlipFillProperties blibFill = new CTBlipFillProperties();
    CTBlip blib = new CTBlip();
    blib.setEmbed("inline-ref");
    blibFill.setBlip(blib);
    pic.setBlipFill(blibFill);
    graphicData.getAny().add(pic);
    graphic.setGraphicData(graphicData);
    inline.setGraphic(graphic);

    drawing.getAnchorOrInline().add(inline);

    JAXBElement<Drawing> element = new JAXBElement<>(new QName("drawing"), Drawing.class, drawing);
    runElement.getContent().add(element);
    return runElement;
  }

  public static Map<String, DocxImagePart> getImageMap() {
    Map<String, DocxImagePart> map = new HashMap<>();
    map.put(
        "inline-ref",
        new DocxImagePart("inline-content-type", "inline".getBytes(StandardCharsets.UTF_8)));
    map.put(
        "anchor-ref",
        new DocxImagePart("anchor-content-type", "anchor".getBytes(StandardCharsets.UTF_8)));
    map.put(
        "vml-ref", new DocxImagePart("vml-content-type", "vml".getBytes(StandardCharsets.UTF_8)));
    return map;
  }

  public static R buildVmlImage() {
    R runElement = new R();
    Pict pict = new Pict();
    CTShape shape = new CTShape();
    CTImageData imageData = new CTImageData();
    imageData.setId("vml-ref");
    shape
        .getEGShapeElements()
        .add(new JAXBElement<>(new QName("image"), CTImageData.class, imageData));
    pict.getAnyAndAny().add(shape);
    runElement.getContent().add(new JAXBElement<>(new QName("pict"), Pict.class, pict));
    return runElement;
  }

  public static class ParagraphBuilder {
    private final List<R> runElements = new ArrayList<>();
    private PPr paragraphStyle;
    private RPr runElementStyle;

    public ParagraphBuilder setParagraphStyle(PPr pPr) {
      paragraphStyle = pPr;
      return this;
    }

    public ParagraphBuilder setRunElementStyle(RPr rPr) {
      runElementStyle = rPr;
      return this;
    }

    public ParagraphBuilder addRunElement(R runElement) {
      if (runElementStyle != null) {
        runElement.setRPr(runElementStyle);
      }
      runElements.add(runElement);
      return this;
    }

    public P build() {
      P paragraph = new P();
      if (paragraphStyle != null) {
        paragraph.setPPr(paragraphStyle);
      }
      runElements.forEach(runElement -> paragraph.getContent().add(runElement));
      return paragraph;
    }
  }
}
