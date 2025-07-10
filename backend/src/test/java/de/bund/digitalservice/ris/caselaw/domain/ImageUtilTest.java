package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.image.ImageRotationAngle;
import de.bund.digitalservice.ris.caselaw.domain.image.ImageUtil;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.stream.Stream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class ImageUtilTest {

  private static final String BASE64_PNG_SOURCE_SRC_TAG =
      "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAIAQMAAAD+wSzIAAAABlBMVEX///+/v7+jQ3Y5AAAADklEQVQI12P4AIX8EAgALgAD/aNpbtEAAAAASUVORK5CYII";

  public static Element getDefaultImageTag() {
    return new Element("img")
        .addClass("inline")
        .addClass("align-baseline")
        .attr("src", BASE64_PNG_SOURCE_SRC_TAG)
        .attr("alt", "Smallest base64 image")
        .attr("width", "82")
        .attr("height", "80");
  }

  @Test
  void test_extractBase64ImageTags() {
    var html = "<p>" + getDefaultImageTag().outerHtml() + "</p>";
    var results = ImageUtil.extractBase64ImageTags(Jsoup.parse(html));
    Assertions.assertFalse(results.isEmpty());
  }

  @Test
  void test_createImageElementWithNewSrc_shouldReplaceSource() {
    var imageTag = getDefaultImageTag();

    String newSrc =
        "/api/v1/caselaw/documentunits/YYTestDoc0001/image/26883193-d749-4003-9940-432b3d87e435.png";

    Assertions.assertNotNull(imageTag);
    Element updatedImageTag =
        ImageUtil.createImageElementWithNewSrc(
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
            () -> ImageUtil.createImageElementWithNewSrc(imageTag, "123.png", null));

    Assertions.assertEquals(
        "Documentation unit number can't be null or blank", thrown.getMessage());
  }

  @Test
  void test_createImageElementWithNewSrcWithEmptyFileName_shouldThrowException() {
    var imageTag = getDefaultImageTag();

    IllegalArgumentException thrown =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> ImageUtil.createImageElementWithNewSrc(imageTag, null, "YYTestDoc0001"));

    Assertions.assertEquals("File name must have a valid image extension", thrown.getMessage());
  }

  @Test
  void test_createImageElementWithNewSrcWithInvalidFileName_shouldThrowException() {
    var imageTag = getDefaultImageTag();

    IllegalArgumentException thrown =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () ->
                ImageUtil.createImageElementWithNewSrc(
                    imageTag, "fileNameWithoutExtension", "YYTestDoc0001"));

    Assertions.assertEquals("File name must have a valid image extension", thrown.getMessage());
  }

  @ParameterizedTest
  @MethodSource("imageFormatsWithFileMagicNumbers")
  void testBase64ToByteEncoder(String base64, byte[] expectedHeader) {
    ByteBuffer result = ImageUtil.encodeToBytes(base64);
    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.remaining() > 0, "Decoded buffer should not be empty");

    byte[] actualHeader = new byte[expectedHeader.length];
    result.get(actualHeader, 0, expectedHeader.length);
    Assertions.assertArrayEquals(expectedHeader, actualHeader);
  }

  /**
   * See {@link <a href="https://gist.github.com/leommoore/f9e57ba2aa4bf197ebc5#image-files">File
   * Magic Numbers</a>}.
   */
  static Stream<org.junit.jupiter.params.provider.Arguments> imageFormatsWithFileMagicNumbers() {

    var expectedJPEGFileTypeStructure = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    return Stream.of(
        // PNG
        org.junit.jupiter.params.provider.Arguments.of(
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA",
            new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}),
        // JPEG
        org.junit.jupiter.params.provider.Arguments.of(
            "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD", expectedJPEGFileTypeStructure),
        // JPG
        org.junit.jupiter.params.provider.Arguments.of(
            "data:image/jpg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD", expectedJPEGFileTypeStructure),
        // GIF
        org.junit.jupiter.params.provider.Arguments.of(
            "data:image/gif;base64,R0lGODlhAQABAIAAAAUEBA==", new byte[] {0x47, 0x49, 0x46, 0x38}));
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
    Assertions.assertEquals(extension, ImageUtil.getFileExtension(src));
  }

  @ParameterizedTest
  @EnumSource(ImageRotationAngle.class)
  void testImageRotation(ImageRotationAngle rotationAngle) {

    int width = 120;
    int height = 80;

    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();

    g.fillRect(0, 0, width, height);

    int red = Color.RED.getRGB();

    // Setting top left red button for assertions after rotation
    image.setRGB(0, 0, red);

    g.dispose();

    Assertions.assertNotNull(image);

    BufferedImage rotated = ImageUtil.rotateImage(image, rotationAngle);

    switch (rotationAngle) {
      case D90 -> {
        Assertions.assertEquals(height, rotated.getWidth());
        Assertions.assertEquals(width, rotated.getHeight());

        // After 90° rotation, (0,0) → (height - 1, 0)
        Assertions.assertEquals(red, rotated.getRGB(height - 1, 0));
      }
      case D180 -> {
        Assertions.assertEquals(width, rotated.getWidth());
        Assertions.assertEquals(height, rotated.getHeight());

        // After 180° rotation, (0,0) → (width - 1, height - 1)
        Assertions.assertEquals(red, rotated.getRGB(width - 1, height - 1));
      }
      case D270 -> {
        Assertions.assertEquals(height, rotated.getWidth());
        Assertions.assertEquals(width, rotated.getHeight());

        // After 270° rotation, (0,0) → (0, width - 1)
        Assertions.assertEquals(red, rotated.getRGB(0, width - 1));
      }
    }
  }
}
