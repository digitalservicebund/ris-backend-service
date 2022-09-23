package de.bund.digitalservice.ris.domain.docx;

public record Style(String property, String value) {
  public String toString() {
    return property + ": " + value;
  }
}
