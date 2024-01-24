package de.bund.digitalservice.ris.caselaw.adapter.converter.docx;

import java.math.BigInteger;

/**
 * Convert some unit formats which are used in the docx specification to screen pixel.
 *
 * <h2>twip</h2>
 *
 * Find more information: <a href="https://en.wikipedia.org/wiki/Twip">twip</a> and <a
 * href="https://www.pixelto.net/px-to-twip-converter">twip converter</a>
 *
 * <ul>
 *   <li>twip is known as twentieth of a point twip is equal to 1/1440 inch
 *   <li>for 96 dpi 1 px = 1 in / 96
 *   <li>1 px = (1440 twip) / 96 Than we get the twip pixel equation
 *   <li>1 px = 15 twip
 * </ul>
 *
 * <h2>EMU (English Metric Unit)</h2>
 *
 * <ul>
 *   <li>1 in = 914400 EMU 1 cm = 360000 EMU
 * </ul>
 */
public class DocxUnitConverter {
  private DocxUnitConverter() {}

  public static int convertTwipToPixel(long twip) {
    return (int) (twip / 15); // 1440 / 96
  }

  public static int convertEMUToPixel(long emu) {
    return (int) (emu / 9525); // 914400 / 96
  }

  public static float convertPointToPixel(BigInteger point) {
    float pixel = Math.max(point.floatValue() / 8.0f, 0.25f);
    pixel = Math.min(pixel, 12.0f);
    return pixel;
  }
}
