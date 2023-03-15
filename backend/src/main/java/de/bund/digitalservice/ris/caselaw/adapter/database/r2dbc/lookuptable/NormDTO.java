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
@Table("lookuptable_field_of_law_norm")
public class NormDTO {

  public static final NormDTO EMPTY = new NormDTO();

  @Id Long id;
  Long fieldOfLawId;
  String abbreviation;
  String singleNormDescription;
}
