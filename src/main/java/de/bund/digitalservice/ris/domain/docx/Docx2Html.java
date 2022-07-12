package de.bund.digitalservice.ris.domain.docx;

public record Docx2Html(String content) {
  public static final Docx2Html EMPTY = new Docx2Html(null);
}
