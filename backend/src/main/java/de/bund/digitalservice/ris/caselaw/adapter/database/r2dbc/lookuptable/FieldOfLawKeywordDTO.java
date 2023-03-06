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
@Table("lookuptable_subject_field_keyword")
public class FieldOfLawKeywordDTO {

  public static final FieldOfLawKeywordDTO EMPTY = new FieldOfLawKeywordDTO();

  @Id Long id;

  Long subjectFieldId;

  String value;
}
