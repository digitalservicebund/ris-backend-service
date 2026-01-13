package de.bund.digitalservice.ris.caselaw.domain;

import java.util.Arrays;
import java.util.Objects;
import lombok.Builder;

@Builder
public record MailAttachmentImage(String fileName, byte[] fileContent) {
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    return o instanceof MailAttachmentImage other && Arrays.equals(other.fileContent, fileContent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(Arrays.hashCode(fileContent));
  }
}
