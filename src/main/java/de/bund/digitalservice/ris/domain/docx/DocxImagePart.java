package de.bund.digitalservice.ris.domain.docx;

import java.util.Arrays;
import java.util.Objects;

public record DocxImagePart(String contentType, byte[] bytes) {
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DocxImagePart that = (DocxImagePart) o;
    return Objects.equals(contentType, that.contentType) && Arrays.equals(bytes, that.bytes);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(contentType);
    result = 31 * result + Arrays.hashCode(bytes);
    return result;
  }

  @Override
  public String toString() {
    return "DocxImagePart{"
        + "contentType='"
        + contentType
        + '\''
        + ", bytes="
        + Arrays.toString(bytes)
        + '}';
  }
}
