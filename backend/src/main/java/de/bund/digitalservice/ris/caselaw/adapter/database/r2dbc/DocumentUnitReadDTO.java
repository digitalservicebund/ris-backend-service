package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("document_unit_with_latest_status")
@EqualsAndHashCode(callSuper = true)
public class DocumentUnitReadDTO extends DocumentUnitWriteDTO {
  public static final DocumentUnitReadDTO EMPTY = new DocumentUnitReadDTO();

  @Column("status")
  String status;
}
