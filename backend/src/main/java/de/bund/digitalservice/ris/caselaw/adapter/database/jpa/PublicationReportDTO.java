package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
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
@Table(name = "publication_report")
public class PublicationReportDTO {
  @Id @GeneratedValue private UUID id;

  @Column(name = "document_unit_id")
  private UUID documentUnitId;

  private String content;

  @Column(name = "received_date")
  private Instant receivedDate;
}
