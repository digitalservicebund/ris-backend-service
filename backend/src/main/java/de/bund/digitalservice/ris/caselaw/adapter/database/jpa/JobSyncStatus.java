package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "job_sync_status", schema = "incremental_migration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobSyncStatus {

  @Id
  @Column(name = "job_name")
  private String jobName; // z.B. "ULI_REVOKED_SYNC"

  @Column(name = "last_run", nullable = false)
  private Instant lastRun;
}
