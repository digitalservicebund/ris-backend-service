package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(schema = "incremental_migration", name = "related_documentation")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class RelatedDocumentationDTO {

  @Id @GeneratedValue private UUID id;

  @Column(name = "court_location", insertable = false, updatable = false)
  private String courtLocation;

  @Column(name = "court_type", insertable = false, updatable = false)
  private String courtType;

  @ManyToOne private CourtDTO court;

  @Column(name = "date")
  private LocalDate date;

  @Column(name = "document_number", insertable = false, updatable = false)
  private String documentNumber;

  @ManyToOne
  @JoinColumn(name = "document_type_id")
  private DocumentTypeDTO documentType;

  @Column(name = "document_type_raw_value", insertable = false, updatable = false)
  private String documentTypeRawValue;

  @Column(name = "file_number")
  private String fileNumber;

  @Column @NotNull private Integer rank;

  //  @ManyToOne private DocumentationUnitDTO documentationUnit;

  //  @Transient private DocumentationUnitDTO referencedDocumentationUnit;
}
