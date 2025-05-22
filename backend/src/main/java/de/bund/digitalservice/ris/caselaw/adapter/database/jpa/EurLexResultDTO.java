package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.eurlex.EurLexResultStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(schema = "incremental_migration", name = "eurlex")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EurLexResultDTO {
  @Id @GeneratedValue private UUID id;

  private String celex;

  private String ecli;

  @Column(name = "file_number")
  private String fileNumber;

  @OneToOne
  @JoinColumn(name = "court_id")
  private CourtDTO court;

  @Column(name = "decision_date")
  private LocalDate date;

  @OneToOne
  @JoinColumn(name = "documentation_unit_id")
  private DocumentationUnitDTO documentationUnit;

  @Column(name = "html_link")
  private String htmlLink;

  private String uri;

  @Enumerated(EnumType.STRING)
  private EurLexResultStatus status;

  @Column(name = "result_xml")
  private String resultXml;

  @Column(name = "created_at", insertable = false, updatable = false)
  private Instant createdAt;
}
