package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("document_unit_status")
public class DocumentUnitStatusDTO implements Persistable<UUID> {
  @Id UUID id;

  @Column("created_at")
  private Instant createdAt;

  private String status;

  @Column("document_unit_id")
  private UUID documentUnitId;

  @Transient private boolean newEntry;

  @Override
  @Transient
  public boolean isNew() {
    return this.newEntry || id == null;
  }
}
