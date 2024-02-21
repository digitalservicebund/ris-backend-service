package de.bund.digitalservice.ris.caselaw.domain;

import java.io.IOException;

public class DocumentationUnitExistsException extends IOException {
  public DocumentationUnitExistsException(String message) {
    super(message);
  }
}
