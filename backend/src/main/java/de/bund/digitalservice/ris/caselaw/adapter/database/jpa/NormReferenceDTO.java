package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "norm_reference", schema = "incremental_migration")
public class NormReferenceDTO {

  @Id @GeneratedValue UUID id;

  @Column(name = "norm_abbreviation")
  String normAbbreviation;

  @Column(name = "single_norm")
  String singleNorm;

  @Column(name = "date_of_version")
  LocalDate dateOfVersion;

  @Column(name = "date_of_relevance")
  String dateOfRelevance;

  @Column(name = "documentation_unit_id")
  @NotNull
  UUID documentUnitId;

  // @ManyToOne @NotNull DocumentUnitDTO documentUnit;
}
