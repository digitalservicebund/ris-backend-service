package de.bund.digitalservice.ris.caselaw.domain;

import java.nio.ByteBuffer;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ServiceUtils {

  public byte[] byteBufferToArray(ByteBuffer byteBuffer) {
    byteBuffer.rewind();
    byte[] byteBufferArray = new byte[byteBuffer.remaining()];
    byteBuffer.get(byteBufferArray);
    byteBuffer.rewind();
    return byteBufferArray;
  }
}
