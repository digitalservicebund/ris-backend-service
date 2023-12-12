package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(schema = "incremental_migration", name = "related_documentation")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class RelatedDocumentationDTO {

  @Include @Id @GeneratedValue private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  private CourtDTO court;

  @Column(name = "date")
  private LocalDate date;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "document_type_id")
  private DocumentTypeDTO documentType;

  @Column(name = "document_type_raw_value", insertable = false, updatable = false)
  private String documentTypeRawValue;

  @Column(name = "file_number")
  private String fileNumber;

  @Column(name = "dtype", updatable = false, insertable = false)
  private RelatedDocumentationType type;

  @Column @NotNull private Integer rank;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "document_number", referencedColumnName = "document_number")
  private DocumentationUnitDTO referencedDocumentationUnit;

  @Include
  @Column(name = "document_number", insertable = false, updatable = false)
  private String documentNumber;
}
