package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("lookuptable_subject_field_norm")
public class NormDTO {

  public static final NormDTO EMPTY = new NormDTO();

  @Id Long id;
  Long subjectFieldId;
  String abbreviation;
  String singleNormDescription;
}
