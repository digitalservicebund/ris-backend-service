package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "published_documentation_snapshot", schema = "incremental_migration")
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PublishedDocumentationSnapshotEntity {
  @Id @GeneratedValue private UUID id;

  @NotNull
  @Column(name = "documentation_unit_id")
  private UUID documentationUnitId;

  @Type(DocumentationUnitType.class)
  private DocumentationUnit json;

  @Column(name = "created_at")
  private LocalDateTime publishedAt;
}
