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
@Table("lookuptable_subject_field")
public class SubjectFieldDTO implements Persistable<Long> {
  public static final SubjectFieldDTO EMPTY = new SubjectFieldDTO();
  @Id Long id;
  Integer childrenCount;
  Long parentId;
  String changeDateMail;
  String changeDateClient;
  Character changeIndicator;
  String version;
  String subjectFieldNumber;
  String subjectFieldText;
  String navigationTerm;
  @Transient List<SubjectFieldDTO> linkedFields;
  @Transient List<FieldOfLawKeywordDTO> keywords;
  @Transient List<NormDTO> norms;

  @Transient private boolean isNew;

  @Override
  public boolean isNew() {
    return this.isNew || id == null;
  }
}
