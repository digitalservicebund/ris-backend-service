package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Image {
  byte[] content;
  String contentType;
  String name;
}
