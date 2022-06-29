package de.bund.digitalservice.ris.domain.docx;

import lombok.Data;

@Data
public class Docx2Html {
  public static final Docx2Html EMPTY = new Docx2Html();

  private String content;
}
