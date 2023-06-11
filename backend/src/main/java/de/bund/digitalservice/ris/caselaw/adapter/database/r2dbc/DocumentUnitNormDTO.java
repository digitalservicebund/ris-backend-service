package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.NormAbbreviationDTO;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("document_unit_norm")
public class DocumentUnitNormDTO {
  public static final DocumentUnitNormDTO EMPTY = new DocumentUnitNormDTO();

  @Id Long id;
  Long documentUnitId;
  UUID normAbbreviationUuid;
  String singleNorm;
  Instant dateOfVersion;
  String dateOfRelevance;
  @Transient NormAbbreviationDTO normAbbreviation;
}
