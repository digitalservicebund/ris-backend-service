package de.bund.digitalservice.ris.caselaw.domain.docx;

/** ECLI element in the footers of the docx file */
public class ECLIElement extends FooterElement {
  public ECLIElement(ParagraphElement paragraph) {
    super(paragraph);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (obj instanceof ECLIElement other) {
      return getText().equals(other.getText());
    }

    return false;
  }

  @Override
  public int hashCode() {
    return getText().hashCode();
  }
}
