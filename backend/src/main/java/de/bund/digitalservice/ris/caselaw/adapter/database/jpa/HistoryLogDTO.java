package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Entity
@Getter
@Table(name = "documentation_unit_history_log", schema = "incremental_migration")
public class HistoryLogDTO {
  @Id
  @Column(nullable = false)
  private UUID id;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "documentation_unit_id", nullable = false)
  private UUID documentationUnitId;

  @OneToOne()
  @JoinColumn(name = "documentation_office", referencedColumnName = "id")
  private DocumentationOfficeDTO documentationOffice;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "user_name")
  private String userName;

  @Column(name = "system_name")
  private String systemName;

  @Column(name = "description")
  private String description;

  @Column(name = "event_type")
  private String eventType;
}
