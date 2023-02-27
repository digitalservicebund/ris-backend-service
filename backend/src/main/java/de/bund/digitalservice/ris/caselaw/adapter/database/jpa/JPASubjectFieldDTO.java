package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "lookuptable_subject_field")
public class JPASubjectFieldDTO {

  @Id Long id;

  @ManyToOne
  @JoinColumn(name = "parent_id", referencedColumnName = "id")
  JPASubjectFieldDTO parentSubjectField;

  @Column(name = "change_date_mail")
  String changeDateMail;

  @Column(name = "change_date_client")
  String changeDateClient;

  @Column(name = "change_indicator")
  char changeIndicator;

  String version;

  @Column(name = "subject_field_number")
  String subjectFieldNumber;

  @Column(name = "subject_field_text")
  String subjectFieldText;

  @Column(name = "navigation_term")
  String navigationTerm;

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "subject_field_id")
  Set<JPAKeywordDTO> keywords;

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "subject_field_id")
  Set<JPANormDTO> norms;

  @Column(name = "children_count")
  Integer childrenCount;

  public String getSubjectFieldNumberOfParent() {
    int lastIndexOf = subjectFieldNumber.lastIndexOf('-');

    if (lastIndexOf == -1) {
      return null;
    }

    return subjectFieldNumber.substring(0, lastIndexOf);
  }
}
