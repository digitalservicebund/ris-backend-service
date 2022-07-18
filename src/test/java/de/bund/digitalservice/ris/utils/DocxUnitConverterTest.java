package de.bund.digitalservice.ris.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocxUnitConverterTest {
  @Test
  void testConvertTwipToPixel() {
    var result = DocxUnitConverter.convertTwipToPixel(1440);

    assertThat(result).isEqualTo(96);
  }

  @Test
  void testConvertEMUToPixel() {
    var result = DocxUnitConverter.convertEMUToPixel(914400);

    assertThat(result).isEqualTo(96);
  }
}
