package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "norm_abbreviation_document_type")
public class NormAbbreviationDocumentTypeDTO implements Persistable<UUID> {
  private UUID normAbbreviationId;
  private UUID documentTypeId;
  @Transient private boolean newEntity = false;

  @Override
  public UUID getId() {
    return null;
  }

  @Override
  public boolean isNew() {
    return newEntity;
  }
}
