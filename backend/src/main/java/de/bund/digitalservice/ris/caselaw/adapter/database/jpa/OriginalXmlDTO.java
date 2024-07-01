package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
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
@Builder(toBuilder = true)
@Entity
@Table(name = "original_xml", schema = "incremental_migration")
public class OriginalXmlDTO {

  @Id private UUID documentationUnitId;

  @Column(name = "created_at")
  @NotNull
  private Instant createdAt;

  @Column(name = "updated_at")
  @NotNull
  private Instant updatedAt;

  @Column @NotBlank private String content;

  @OneToOne
  @MapsId
  @JoinColumn(name = "documentation_unit_id")
  private DocumentationUnitDTO documentationUnit;
}
