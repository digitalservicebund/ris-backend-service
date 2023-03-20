package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision;

import java.time.Instant;
import java.util.UUID;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Builder(toBuilder = true)
@Table("doc_unit")
public record ProceedingDecisionDTO (
  @Id Long id,

  UUID uuid,

  @Column("gerichtstyp")
  String courtType,

  @Column("gerichtssitz")
  String courtLocation,

  @Column("decision_date")
  Instant decisionDate,

  @Transient String fileNumber,

  @Column("document_type_id") Long documentTypeId, // points to lookup table row id
  @Transient DocumentTypeDTO documentTypeDTO
){}
