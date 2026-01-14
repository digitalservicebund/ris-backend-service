package de.bund.digitalservice.ris.caselaw.domain;

import java.util.Arrays;
import java.util.Objects;
import lombok.Builder;
import org.jspecify.annotations.NonNull;

@Builder
public record MailAttachmentImage(String fileName, byte[] fileContent) {
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MailAttachmentImage other = (MailAttachmentImage) o;
    return Arrays.equals(other.fileContent, fileContent)
        && Objects.equals(fileName, other.fileName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(Arrays.hashCode(fileContent));
  }

  @Override
  public @NonNull String toString() {
    return fileName;
  }
}
