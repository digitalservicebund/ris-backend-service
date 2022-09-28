package de.bund.digitalservice.ris.domain.docx;

import java.util.List;

public record Style(String property, List<String> value) {
  public String toString() {
    return property + ": " + String.join(", ", value);
  }
}
