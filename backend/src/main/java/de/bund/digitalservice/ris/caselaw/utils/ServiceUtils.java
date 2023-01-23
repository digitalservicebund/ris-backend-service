package de.bund.digitalservice.ris.caselaw.utils;

import java.nio.ByteBuffer;

public class ServiceUtils {

  public static byte[] byteBufferToArray(ByteBuffer byteBuffer) {
    byteBuffer.rewind();
    byte[] byteBufferArray = new byte[byteBuffer.remaining()];
    byteBuffer.get(byteBufferArray);
    byteBuffer.rewind();
    return byteBufferArray;
  }
}
