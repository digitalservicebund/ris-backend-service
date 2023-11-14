package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "status")
public class StatusDTO {
  @Id @GeneratedValue UUID id;

  @Column(name = "created_at")
  @GeneratedValue
  private Instant createdAt;

  @Column(name = "publication_status")
  @NotNull
  private PublicationStatus publicationStatus;

  @Column(name = "with_error")
  @NotNull
  private boolean withError;

  @ManyToOne
  @JoinColumn(name = "document_unit_id")
  DocumentationUnitDTO documentationUnitDTO;

  @Column(name = "issuer_address")
  private String issuerAddress;
}
