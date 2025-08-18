package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "documentation_unit_history_log", schema = "incremental_migration")
public class HistoryLogDTO {
  @Id
  @GeneratedValue
  @Column(nullable = false)
  private UUID id;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "documentation_unit_id", nullable = false)
  private UUID documentationUnitId;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "system_name")
  private String systemName;

  @Column(name = "description")
  private String description;

  @Column(name = "event_type")
  @Enumerated(EnumType.STRING)
  private HistoryLogEventType eventType;
}
