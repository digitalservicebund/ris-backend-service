package de.bund.digitalservice.ris.caselaw.domain.exception;

import java.io.IOException;

public class DocumentationUnitExistsException extends IOException {
  public DocumentationUnitExistsException(String message) {
    super(message);
  }
}
