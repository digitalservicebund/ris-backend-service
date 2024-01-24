package de.bund.digitalservice.ris.caselaw.domain.docx;

/** Footer element in the docx file */
public class FooterElement extends ParagraphElement {

  private final ParagraphElement paragraph;

  public FooterElement(ParagraphElement paragraph) {
    this.paragraph = paragraph;
  }

  @Override
  public String toHtmlString() {
    return paragraph.toHtmlString();
  }

  @Override
  public String getText() {
    return paragraph.getText();
  }
}
