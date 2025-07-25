package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documentation_office", schema = "incremental_migration")
public class DocumentationOfficeDTO {
  @Id private UUID id;

  @Column(name = "abbreviation")
  private String abbreviation;

  @OneToOne
  @JoinColumn(name = "jurisdiction_type_id")
  private JurisdictionTypeDTO jurisdictionType;

  @ManyToMany(
      cascade = {CascadeType.MERGE},
      fetch = FetchType.LAZY)
  @JoinTable(
      name = "process_step_documentation_office",
      schema = "incremental_migration",
      joinColumns = @JoinColumn(name = "documentation_office_id"),
      inverseJoinColumns = @JoinColumn(name = "process_step_id"))
  @OrderColumn(name = "rank")
  @Builder.Default
  private List<ProcessStepDTO> processSteps = new ArrayList<>();
}
