package de.bund.digitalservice.ris.caselaw.domain.image;

import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.ImageConstants;
import org.freehep.graphicsio.ImageGraphics2D;
import org.freehep.graphicsio.emf.EMFInputStream;
import org.freehep.graphicsio.emf.EMFPanel;
import org.freehep.graphicsio.emf.EMFRenderer;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.MediaType;

/** * Utility class for processing and converting image data */
@UtilityClass
@Slf4j
public class ImageUtil {

  public static final String BASE64_PNG_UNKNOWN_FILE_ICON =
      "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";

  public static Elements extractBase64ImageTags(Document document) {
    return document.select("img[src^=data:image/]");
  }

  public static Element createImageElementWithNewSrc(
      Element oldElement, String fileName, String documentNumber) {
    assertHasValidImageExtension(fileName);
    if (StringUtils.isNullOrBlank(documentNumber)) {
      throw new IllegalArgumentException("Documentation unit number can't be null or blank");
    }

    var src = "/api/v1/caselaw/documentunits/" + documentNumber + "/image/" + fileName;
    return oldElement.clone().attr("src", src);
  }

  public static void assertHasValidImageExtension(String fileName) {
    if (StringUtils.isNullOrBlank(fileName) || !fileName.contains(".")) {
      throw new IllegalArgumentException("File name must have a valid image extension");
    }
  }

  public static List<MediaType> getSupportedMediaTypes() {
    return List.of(MediaType.IMAGE_GIF, MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG);
  }

  public static ByteBuffer encodeToBytes(Element imageTag) {
    return encodeToBytes(imageTag.attr("src"));
  }

  public static ByteBuffer encodeToBytes(String base64SourceTag) {
    String base64DataString = base64SourceTag.split(",")[1].trim();
    return ByteBuffer.wrap(Base64.getDecoder().decode(base64DataString));
  }

  public static String getFileExtension(Element imageTag) {
    return getFileExtension(imageTag.attr("src"));
  }

  public static String getFileExtension(String base64SourceTag) {
    String mimePrefix = base64SourceTag.split(",")[0];
    return mimePrefix.split("/")[1].split(";")[0].toLowerCase();
  }

  public static byte[] convertEMF2PNG(byte[] originalBytes, Dimension size) {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      EMFInputStream emf = new EMFInputStream(new ByteArrayInputStream(originalBytes));
      EMFRenderer renderer = new EMFRenderer(emf);

      EMFPanel emfPanel = new EMFPanel();
      emfPanel.setRenderer(renderer);

      size = new Dimension((int) size.getWidth() * 2, (int) size.getHeight() * 2);

      double scaleX = size.getWidth() / emfPanel.getWidth();
      double scaleY = size.getHeight() / emfPanel.getHeight();

      VectorGraphics g = new ImageGraphics2D(outputStream, size, ImageConstants.PNG);
      g.scale(scaleX, scaleY);

      g.startExport();
      emfPanel.print(g);
      g.endExport();

      outputStream.flush();

      return outputStream.toByteArray();
    } catch (Exception ex) {
      log.error("Couldn't convert emf to png", ex);
    }

    return originalBytes;
  }

  public static BufferedImage rotateImage(
      BufferedImage image, ImageRotationAngle imageRotationAngle) {

    var degree = imageRotationAngle.getRadians();

    int width = image.getWidth();
    int height = image.getHeight();
    int newWidth =
        (int) Math.abs(width * Math.cos(degree)) + (int) Math.abs(height * Math.sin(degree));
    int newHeight =
        (int) Math.abs(height * Math.cos(degree)) + (int) Math.abs(width * Math.sin(degree));

    BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, image.getType());

    AffineTransform transform = new AffineTransform();
    transform.rotate(degree, (double) newWidth / 2, (double) newHeight / 2);

    transform.translate((double) (newWidth - width) / 2, (double) (newHeight - height) / 2);

    Graphics2D g2d = rotatedImage.createGraphics();
    g2d.setTransform(transform);
    g2d.drawImage(image, 0, 0, null);
    g2d.dispose();

    return rotatedImage;
  }
}
