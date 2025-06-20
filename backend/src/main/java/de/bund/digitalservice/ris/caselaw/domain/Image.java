package de.bund.digitalservice.ris.caselaw.domain;

import java.util.Arrays;
import java.util.Objects;
import lombok.Builder;

@Builder
public record Image(byte[] content, String contentType, String name) {

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Image image = (Image) o;
    return Arrays.equals(content, image.content)
        && Objects.equals(contentType, image.contentType)
        && Objects.equals(name, image.name);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(contentType, name);
    result = 31 * result + Arrays.hashCode(content);
    return result;
  }

  @Override
  public String toString() {
    return "Image{"
        + "contentSize="
        + (content != null ? content.length : "null")
        + ", contentType='"
        + contentType
        + '\''
        + ", name='"
        + name
        + '\''
        + '}';
  }
}
