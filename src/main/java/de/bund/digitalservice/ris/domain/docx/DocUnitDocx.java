package de.bund.digitalservice.ris.domain.docx;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public interface DocUnitDocx {
  String toHtmlString();
}
