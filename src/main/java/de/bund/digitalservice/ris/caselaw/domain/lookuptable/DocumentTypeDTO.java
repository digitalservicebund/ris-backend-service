package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("lookuptable_documenttype")
public class DocumentTypeDTO {
  long id;
  String changeDateMail;
  String changeDateClient;
  char changeIndicator;
  String version;
  String jurisShortcut;
  char documentType;
  String multiple;
  String label;
  String superlabel1;
  String superlabel2;
}
