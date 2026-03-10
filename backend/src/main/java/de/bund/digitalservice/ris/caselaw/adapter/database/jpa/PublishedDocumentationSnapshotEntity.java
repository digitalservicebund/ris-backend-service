package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import io.hypersistence.utils.hibernate.type.json.JsonType;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "published_documentation_snapshot", schema = "incremental_migration")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class PublishedDocumentationSnapshotEntity {
  @Id @GeneratedValue private UUID id;

  @NotNull
  @Column(name = "documentation_unit_id")
  private UUID documentationUnitId;

  @Column(name = "json", columnDefinition = "jsonb")
  @Type(JsonType.class)
  private JsonNode json;

  @Column(name = "last_updated_at")
  private LocalDateTime lastUpdatedAt;
}
