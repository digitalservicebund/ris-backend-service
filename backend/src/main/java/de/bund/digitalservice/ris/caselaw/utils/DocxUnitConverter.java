package de.bund.digitalservice.ris.caselaw.utils;

import java.math.BigInteger;

/**
 * TWIP
 *
 * <p>https://en.wikipedia.org/wiki/Twip https://www.pixelto.net/px-to-twip-converter
 *
 * <p>twip is known as twentieth of a point twip is equal to 1/1440 inch
 *
 * <p>for 96 dpi 1 px = 1 in / 96
 *
 * <p>1 px = (1440 twip) / 96 Than we get the twip pixel equation
 *
 * <p>1px = 15 twip
 *
 * <p>EMU (English Metric Unit
 *
 * <p>1 in = 914400 EMU 1 cm = 360000 EMU
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
