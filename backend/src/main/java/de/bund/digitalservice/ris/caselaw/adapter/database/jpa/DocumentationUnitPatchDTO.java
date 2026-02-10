package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documentation_unit_patch")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DocumentationUnitPatchDTO {
  @Id @GeneratedValue private UUID id;

  @Column(name = "documentation_unit_id")
  private UUID documentationUnitId;

  @Column(name = "documentation_unit_version")
  private Long documentationUnitVersion;

  private String patch;
}
