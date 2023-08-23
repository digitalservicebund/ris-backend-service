package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "search_documentation_unit")
public class DocumentationUnitSearchEntryDTO {
  @Id private UUID uuid;

  // for sorting at the moment
  private Long id;

  @Column(name = "document_number")
  private String documentNumber;

  @Enumerated(value = EnumType.STRING)
  @Column(name = "data_source")
  private DataSource dataSource;

  @Column(name = "file_name")
  private String fileName;

  @Column(name = "court_type")
  private String courtType;

  @Column(name = "court_location")
  private String courtLocation;

  @Column(name = "decision_date")
  private Instant decisionDate;

  @Column(name = "documentation_office_id")
  private UUID documentationOfficeId;

  @Enumerated(value = EnumType.STRING)
  @Column(name = "publication_status")
  private PublicationStatus publicationStatus;

  @Column(name = "with_error")
  private Boolean withError;

  @Column(name = "first_file_number")
  private String firstFileNumber;

  @Column(name = "document_type")
  private String documentType;
}
