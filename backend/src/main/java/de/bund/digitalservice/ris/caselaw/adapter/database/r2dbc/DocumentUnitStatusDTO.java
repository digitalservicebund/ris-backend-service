package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("document_unit_status")
public class DocumentUnitStatusDTO {

  @Id UUID id;

  @Column("created_at")
  Instant createdAt;

  String status;

  @Column("document_unit_id")
  UUID documentUnitId;
}
