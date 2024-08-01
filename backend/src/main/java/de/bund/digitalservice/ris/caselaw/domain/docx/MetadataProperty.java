package de.bund.digitalservice.ris.caselaw.domain.docx;

/** Property element of the docx file */
public class MetadataProperty implements DocumentUnitDocx {

  private DocXPropertyField key;
  private final String value;

  public MetadataProperty(DocXPropertyField key, String value) {
    this.key = key;
    this.value = value;
  }

  @Override
  public String toHtmlString() {
    return null;
  }

  public DocXPropertyField getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }
}
