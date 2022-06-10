package de.bund.digitalservice.ris.datamodel.docx;

import lombok.Data;

@Data
public class Docx2Html {
  public static final Docx2Html EMPTY = new Docx2Html();

  private String fileName;
  private String content;
}
