package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Include;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "documentation_unit", schema = "incremental_migration")
@SuppressWarnings(
    "java:S6539") // This class depends on many classes, because it's the key part and merging
// everything.
public abstract class DocumentationUnitDTO implements DocumentationUnitListItemDTO {

  @Id @GeneratedValue @Include private UUID id;

  private Long version;

  @Column private LocalDate date;

  @Column(nullable = false, unique = true, updatable = false, name = "document_number")
  @NotBlank
  @Include
  private String documentNumber;

  @ManyToOne
  @JoinColumn(name = "document_type_id")
  private DocumentTypeDTO documentType;

  // Aktenzeichen
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  @OrderBy("rank")
  private List<FileNumberDTO> fileNumbers = new ArrayList<>();

  // Titelzeile
  @Column private String headline;

  // Spruchkörper
  @Column(name = "judicial_body")
  private String judicialBody;

  // Normen
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  @OrderBy("rank")
  private List<NormReferenceDTO> normReferences = new ArrayList<>();

  @Builder.Default
  @OneToMany(
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      mappedBy = "documentationUnit")
  @OrderBy("uploadTimestamp asc")
  private List<AttachmentDTO> attachments = new ArrayList<>();

  @ManyToMany(
      cascade = {CascadeType.MERGE},
      fetch = FetchType.LAZY)
  @JoinTable(
      name = "documentation_unit_region",
      schema = "incremental_migration",
      joinColumns = @JoinColumn(name = "documentation_unit_id"),
      inverseJoinColumns = @JoinColumn(name = "region_id"))
  @Builder.Default
  private List<RegionDTO> regions = new ArrayList<>();

  // Sachgebiete
  @OneToMany(
      mappedBy = "documentationUnit",
      orphanRemoval = true,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @OrderBy("rank")
  @Builder.Default
  private List<DocumentationUnitFieldOfLawDTO> documentationUnitFieldsOfLaw = new ArrayList<>();

  @ManyToOne(optional = false)
  @NotNull
  @JoinColumn(name = "documentation_office_id", referencedColumnName = "id")
  private DocumentationOfficeDTO documentationOffice;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(
      name = "documentation_unit_id",
      nullable = false,
      insertable = false,
      updatable = false)
  @Builder.Default
  @OrderBy("createdAt desc")
  private List<StatusDTO> statusHistory = new ArrayList<>();

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "current_status_id")
  private StatusDTO status;

  // Gericht
  @ManyToOne
  @JoinColumn(name = "court_id", referencedColumnName = "id")
  private CourtDTO court;

  // Abweichendes Datum
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  @OrderBy("rank")
  private List<DeviatingDateDTO> deviatingDates = new ArrayList<>();

  //
  //  @OneToMany(
  //      mappedBy = "documentationUnit",
  //      cascade = CascadeType.ALL,
  //      cascade = CascadeType.ALL,
  //      fetch = FetchType.EAGER,
  //      orphanRemoval = true)
  //  @Builder.Default
  //  private Set<DeviatingDocumentNumber> deviatingDocumentNumbers = new HashSet<>();
  //
  // Abweichendes Aktenzeichen
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  @OrderBy("rank")
  private List<DeviatingFileNumberDTO> deviatingFileNumbers = new ArrayList<>();

  // Abweichendes Gericht
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  @OrderBy("rank")
  private List<DeviatingCourtDTO> deviatingCourts = new ArrayList<>();

  // Schlagworte
  @OneToMany(
      mappedBy = "documentationUnit",
      orphanRemoval = true,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @OrderBy("rank")
  @Builder.Default
  private List<DocumentationUnitKeywordDTO> documentationUnitKeywordDTOs = new ArrayList<>();

  // Vorgehende Entscheidungen
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  @OrderBy("rank")
  private List<PreviousDecisionDTO> previousDecisions = new ArrayList<>();

  // Rechtsprechungsfundstellen
  @OneToMany(mappedBy = "documentationUnit", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  @OrderBy("documentationUnitRank")
  private List<CaselawReferenceDTO> caselawReferences = new ArrayList<>();

  // Literaturfundstellen
  @OneToMany(mappedBy = "documentationUnit", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  @OrderBy("documentationUnitRank")
  private List<LiteratureReferenceDTO> literatureReferences = new ArrayList<>();

  @Column(name = "last_publication_date_time")
  private LocalDateTime lastPublicationDateTime;

  @Column(name = "scheduled_publication_date_time")
  private LocalDateTime scheduledPublicationDateTime;

  @Column(name = "scheduled_by_email")
  private String scheduledByEmail;

  @OneToMany(mappedBy = "documentationUnit1", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JsonManagedReference // Prevent infinite recursion
  @Builder.Default
  private Set<DuplicateRelationDTO> duplicateRelations1 = new HashSet<>();

  @OneToMany(mappedBy = "documentationUnit2", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JsonManagedReference // Prevent infinite recursion
  @Builder.Default
  private Set<DuplicateRelationDTO> duplicateRelations2 = new HashSet<>();

  /**
   * This field represents the "Dupcode ausschalten" functionality from the jDV. It is set to false
   * in the migration if the duplicate check should be ignored.
   */
  @Column(name = "duplicate_check")
  private Boolean isJdvDuplicateCheckActive;

  @Column private String fedst;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  @Valid
  private List<DocumentalistDTO> documentalists = new ArrayList<>();
}
