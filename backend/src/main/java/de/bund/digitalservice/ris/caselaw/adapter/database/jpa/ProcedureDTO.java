package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
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
@Entity()
@Table(name = "procedure", schema = "public")
public class ProcedureDTO {
  @Id @GeneratedValue private UUID id;

  @Column(name = "name")
  String label;

  @Column(name = "created_at", updatable = false, insertable = false)
  Instant createdAt;

  @ManyToOne()
  @JoinColumn(name = "documentation_office_id")
  @NotNull
  DocumentationOfficeDTO documentationOffice;

  @ManyToMany()
  @JoinTable(
      name = "procedure_link",
      schema = "public",
      joinColumns = @JoinColumn(name = "procedure_id"))
  List<DocumentationUnitDTO> documentationUnits;
}
