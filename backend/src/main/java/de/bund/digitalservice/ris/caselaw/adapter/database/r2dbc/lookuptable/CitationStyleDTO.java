package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import java.time.LocalDate;
import java.util.UUID;
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
@Table("citation_style")
public class CitationStyleDTO implements Persistable<UUID> {
  public static final CitationStyleDTO EMPTY = new CitationStyleDTO();
  @Id UUID uuid;
  Long jurisId;
  Character changeIndicator;
  LocalDate changeDateMail;
  String version;
  String documentType;
  String citationDocumentType;
  String jurisShortcut;
  String label;

  @Transient private boolean newEntry;

  @Override
  public UUID getId() {
    return this.uuid;
  }

  @Override
  @Transient
  public boolean isNew() {
    return this.newEntry || this.uuid == null;
  }
}
