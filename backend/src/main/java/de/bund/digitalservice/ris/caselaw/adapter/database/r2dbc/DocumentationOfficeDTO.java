package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

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
@Table("documentation_office")
public class DocumentationOfficeDTO implements Persistable<UUID> {
  @Id UUID id;

  private String label;

  private String abbreviation;

  @Transient private boolean newEntry;

  @Override
  @Transient
  public boolean isNew() {
    return this.newEntry || this.id == null;
  }
}
