package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.InboxStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Include;
import lombok.experimental.SuperBuilder;
import org.hibernate.proxy.HibernateProxy;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "document_type_id")
  private DocumentTypeDTO documentType;

  // Aktenzeichen
  @OneToMany(
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true,
      mappedBy = "documentationUnit")
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
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "court_id", referencedColumnName = "id")
  private CourtDTO court;

  // Abweichendes Datum
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  @OrderBy("rank")
  private List<DeviatingDateDTO> deviatingDates = new ArrayList<>();

  @OneToMany(
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true,
      mappedBy = "documentationUnit")
  @Builder.Default
  @OrderBy("rank")
  private List<DeviatingDocumentNumberDTO> deviatingDocumentNumbers = new ArrayList<>();

  // Abweichendes Aktenzeichen
  @OneToMany(
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true,
      mappedBy = "documentationUnit")
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
  @OneToMany(
      mappedBy = "documentationUnit",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  @OrderBy("documentationUnitRank")
  private List<CaselawReferenceDTO> caselawReferences = new ArrayList<>();

  // Literaturfundstellen
  @OneToMany(
      mappedBy = "documentationUnit",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  @OrderBy("documentationUnitRank")
  private List<LiteratureReferenceDTO> literatureReferences = new ArrayList<>();

  @Column(name = "last_publication_date_time")
  private LocalDateTime lastPublicationDateTime;

  @Column(name = "scheduled_publication_date_time")
  private LocalDateTime scheduledPublicationDateTime;

  @Column(name = "scheduled_by_email")
  private String scheduledByEmail;

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

  @OneToOne(mappedBy = "documentationUnit", cascade = CascadeType.ALL, orphanRemoval = true)
  @PrimaryKeyJoinColumn
  private ManagementDataDTO managementData;

  @Column(name = "inbox_status")
  @Nullable
  @Enumerated(EnumType.STRING)
  private InboxStatus inboxStatus;

  @Override
  @Nullable
  /*
   Default implementation for non-PendingProceedingDTOs, needed by
  * DocumentationUnitListItemDTO
  */
  public LocalDate getResolutionDate() {
    return null;
  }

  @Override
  @SuppressWarnings("java:S2097") // Class type check is not recognized by Sonar
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass =
        o instanceof HibernateProxy hibernateProxy
            ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
    Class<?> thisEffectiveClass =
        this instanceof HibernateProxy hibernateProxy
            ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    DocumentationUnitDTO that = (DocumentationUnitDTO) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return this instanceof HibernateProxy hibernateProxy
        ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
