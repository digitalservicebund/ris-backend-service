package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** An interface representing a non-application norm (Nichtanwendungsgesetz) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "non_application_norm", schema = "incremental_migration")
public class NonApplicationNormDTO {

  @Id @GeneratedValue UUID id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "norm_abbreviation_id")
  private NormAbbreviationDTO normAbbreviation;

  @Column(name = "single_norm")
  String singleNorm;

  @Column(name = "date_of_version")
  LocalDate dateOfVersion;

  @Column(name = "date_of_relevance")
  String dateOfRelevance;

  @Column(name = "rank")
  private Integer rank;

  @Transient
  public boolean isSingleNormEmpty() {
    return singleNorm == null && dateOfVersion == null && dateOfRelevance == null;
  }
}
