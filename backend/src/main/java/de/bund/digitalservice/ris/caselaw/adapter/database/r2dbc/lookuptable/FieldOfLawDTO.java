package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("lookuptable_field_of_law")
public class FieldOfLawDTO implements Persistable<Long> {
  public static final FieldOfLawDTO EMPTY = new FieldOfLawDTO();
  @Id Long id;
  Integer childrenCount;
  Long parentId;
  String changeDateMail;
  String changeDateClient;
  Character changeIndicator;
  String version;
  String identifier;
  String text;
  String navigationTerm;
  @Transient List<FieldOfLawDTO> linkedFieldsOfLaw;
  @Transient List<FieldOfLawKeywordDTO> keywords;
  @Transient List<NormDTO> norms;

  @Transient private boolean isNew;

  @Override
  public boolean isNew() {
    return this.isNew || id == null;
  }
}
