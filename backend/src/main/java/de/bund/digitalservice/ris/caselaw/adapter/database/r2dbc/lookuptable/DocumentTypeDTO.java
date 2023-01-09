package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("lookuptable_documenttype")
public class DocumentTypeDTO implements Persistable<Long> {
  public static final DocumentTypeDTO EMPTY = new DocumentTypeDTO();
  @Id Long id;
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

  @Transient private boolean newEntry;

  @Override
  @Transient
  public boolean isNew() {
    return this.newEntry || id == null;
  }
}
