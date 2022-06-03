package de.bund.digitalservice.ris.datamodel.docx;

import lombok.Data;

@Data
public class DocUnitTable implements DocUnitDocx {
  private String textContent;

  @Override
  public String toString() {
    return textContent;
  }

  @Override
  public String toHtmlString() {
    return textContent;
  }
}
