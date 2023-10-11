package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(value = "norm_reference", schema = "incremental_migration")
public class NormReferenceDTO {

  @Id @GeneratedValue UUID id;

  @NotBlank @Column String normAbbreviation;

  @Column String singleNorm;

  @Column LocalDate dateOfVersion;

  @Column String dateOfRelevance;

  @NotNull UUID documentUnitId;

  // @ManyToOne @NotNull DocumentUnitDTO documentUnit;
}
