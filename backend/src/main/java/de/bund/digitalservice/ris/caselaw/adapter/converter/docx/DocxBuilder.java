package de.bund.digitalservice.ris.caselaw.adapter.converter.docx;

import de.bund.digitalservice.ris.caselaw.domain.docx.DocumentUnitDocx;

public abstract class DocxBuilder {
  protected DocxConverter converter;

  public DocxBuilder setConverter(DocxConverter converter) {
    this.converter = converter;
    return this;
  }

  public abstract DocumentUnitDocx build();
}
