package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

  @Column private String courtLocation;

  @Column private String courtType;

  @ManyToOne(fetch = FetchType.LAZY)
  private CourtDTO court;

  @Column private LocalDate date;

  @Column private String documentNumber;

  @ManyToOne private DocumentTypeDTO documentType;

  @Column private String documentTypeRawValue;

  @Column private String fileNumber;

  @ManyToOne private DocumentationUnitDTO documentationUnit;
}
