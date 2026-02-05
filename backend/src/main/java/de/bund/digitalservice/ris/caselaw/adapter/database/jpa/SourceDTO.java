package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.SourceValue;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(schema = "incremental_migration", name = "source")
public class SourceDTO {
  @Id @GeneratedValue private UUID id;

  @NotNull private Integer rank;

  @Enumerated(EnumType.STRING)
  private SourceValue value;

  @Size(max = 1000)
  @Column(name = "source_raw_value")
  private String sourceRawValue;

  // The doc unit also has a direct link to the references and is responsible for updating the
  // references (via cascading) -> No CascadeType.MERGE!
  @OneToOne(
      cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
      orphanRemoval = true)
  @JoinColumn(name = "reference_id")
  private ReferenceDTO reference;

  @OneToOne(
      cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
      orphanRemoval = true)
  @JoinColumn(name = "literature_reference_id")
  private PassiveCitationUliDTO literatureReference;
}
