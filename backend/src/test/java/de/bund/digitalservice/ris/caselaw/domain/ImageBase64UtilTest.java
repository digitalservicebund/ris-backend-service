package de.bund.digitalservice.ris.caselaw.domain;

import java.nio.ByteBuffer;
import java.util.stream.Stream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class ImageBase64UtilTest {

  private static final String base64PNGSourceSrcTag =
      "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAIAQMAAAD+wSzIAAAABlBMVEX///+/v7+jQ3Y5AAAADklEQVQI12P4AIX8EAgALgAD/aNpbtEAAAAASUVORK5CYII";

  public static Element getDefaultImageTag() {
    return new Element("img")
        .addClass("inline")
        .addClass("align-baseline")
        .attr("src", base64PNGSourceSrcTag)
        .attr("alt", "Smallest base64 image")
        .attr("width", "82")
        .attr("height", "80");
  }

  @Test
  void test_extractBase64ImageTags() {
    var html = "<p>" + getDefaultImageTag().outerHtml() + "</p>";
    var results = ImageBase64Util.extractBase64ImageTags(Jsoup.parse(html));
    Assertions.assertFalse(results.isEmpty());
  }

  @Test
  void test_createImageElementWithNewSrc_shouldReplaceSource() {
    var imageTag = getDefaultImageTag();

    String newSrc =
        "/api/v1/caselaw/documentunits/YYTestDoc0001/image/26883193-d749-4003-9940-432b3d87e435.png";

    Assertions.assertNotNull(imageTag);
    Element updatedImageTag =
        ImageBase64Util.createImageElementWithNewSrc(
            imageTag, "26883193-d749-4003-9940-432b3d87e435.png", "YYTestDoc0001");

    Assertions.assertNotNull(updatedImageTag);
    Assertions.assertEquals("img", updatedImageTag.tagName());
    Assertions.assertEquals(newSrc, updatedImageTag.attr("src"));
    Assertions.assertEquals("inline align-baseline", updatedImageTag.className());
    Assertions.assertEquals("Smallest base64 image", updatedImageTag.attr("alt"));
    Assertions.assertEquals("82", updatedImageTag.attr("width"));
    Assertions.assertEquals("80", updatedImageTag.attr("height"));
  }

  @Test
  void test_createImageElementWithNewSrcWithEmptyDocNumber_shouldThrowException() {
    var imageTag = getDefaultImageTag();

    IllegalArgumentException thrown =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> ImageBase64Util.createImageElementWithNewSrc(imageTag, "123.png", null));

    Assertions.assertEquals(
        "Documentation unit number can't be null or blank", thrown.getMessage());
  }

  @Test
  void test_createImageElementWithNewSrcWithEmptyFileName_shouldThrowException() {
    var imageTag = getDefaultImageTag();

    IllegalArgumentException thrown =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> ImageBase64Util.createImageElementWithNewSrc(imageTag, null, "YYTestDoc0001"));

    Assertions.assertEquals("File name must have a valid image extension", thrown.getMessage());
  }

  @Test
  void test_createImageElementWithNewSrcWithInvalidFileName_shouldThrowException() {
    var imageTag = getDefaultImageTag();

    IllegalArgumentException thrown =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () ->
                ImageBase64Util.createImageElementWithNewSrc(
                    imageTag, "fileNameWithoutExtension", "YYTestDoc0001"));

    Assertions.assertEquals("File name must have a valid image extension", thrown.getMessage());
  }

  @ParameterizedTest
  @MethodSource("imageFormatsWithFileMagicNumbers")
  void testBase64ToByteEncoder(String base64, byte[] expectedHeader) {
    ByteBuffer result = ImageBase64Util.encodeToBytes(base64);
    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.remaining() > 0, "Decoded buffer should not be empty");

    byte[] actualHeader = new byte[expectedHeader.length];
    result.get(actualHeader, 0, expectedHeader.length);
    Assertions.assertArrayEquals(expectedHeader, actualHeader);
  }

  static Stream<org.junit.jupiter.params.provider.Arguments> imageFormatsWithFileMagicNumbers() {
    return Stream.of(
        // PNG
        org.junit.jupiter.params.provider.Arguments.of(
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA",
            new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}),
        // JPEG, JFIF
        org.junit.jupiter.params.provider.Arguments.of(
            "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD",
            new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
        // JPG / JPEG
        org.junit.jupiter.params.provider.Arguments.of(
            "data:image/jpg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD",
            new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
        // GIF
        org.junit.jupiter.params.provider.Arguments.of(
            "data:image/gif;base64,R0lGODlhAQABAIAAAAUEBA==",
            new byte[] {0x47, 0x49, 0x46, 0x38} // GIF8
            ));
  }

  @ParameterizedTest
  @CsvSource({
    // Valid cases
    "png, data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA",
    "jpeg, data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD",
    "gif, data:image/gif;base64,R0lGODdhPQBEAPeoAJosM//AwO/AwHV",
    "webp, data:image/webp;base64,UklGRiIAAABXRUJQVlA4TAYAAAAvAAAAHwA",
    "bmp, data:image/bmp;base64,Qk2eAAAAAAAAADYAAAAoAAAAAQAAAP",

    // Variants with no spaces
    "png,data:image/png;base64,iVBORw",
    "jpeg,data:image/jpeg;base64,/9j/4AAQSkZ",

    // Mixed casing
    "png, data:image/PNG;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA",
    "jpeg, data:image/JPEG;base64,/9j/4AAQSkZJRgABAQEAYABgAAD",
  })
  void testGetFileExtension(String extension, String src) {
    Assertions.assertEquals(extension, ImageBase64Util.getFileExtension(src));
  }
}
