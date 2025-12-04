package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/** An interface representing a norm reference, linked to a documentation unit. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
@Entity
@Table(name = "norm_reference", schema = "incremental_migration")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class AbstractNormReferenceDTO {

  @Id @GeneratedValue UUID id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "norm_abbreviation_id")
  private NormAbbreviationDTO normAbbreviation;

  @Column(name = "norm_abbreviation_raw_value")
  private String normAbbreviationRawValue;

  @Column(name = "single_norm")
  String singleNorm;

  @Column(name = "date_of_version")
  LocalDate dateOfVersion;

  @Column(name = "date_of_relevance")
  String dateOfRelevance;

  @Column(name = "rank")
  private Integer rank;

  @OneToOne(
      mappedBy = "normReference",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  private LegalForceDTO legalForce;

  @Transient
  public boolean isSingleNormEmpty() {
    return singleNorm == null
        && dateOfVersion == null
        && dateOfRelevance == null
        && legalForce == null;
  }
}
