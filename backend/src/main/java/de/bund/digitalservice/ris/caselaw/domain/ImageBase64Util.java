package de.bund.digitalservice.ris.caselaw.domain;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.MediaType;

@UtilityClass
public class ImageBase64Util {

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
}
