package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "documentation_unit", schema = "incremental_migration")
public class DocumentationUnitDTO implements DocumentationUnitMetadataDTO {

  @Id @GeneratedValue private UUID id;

  @Column(name = "case_facts")
  private String caseFacts;

  @Column(name = "decision_date")
  private LocalDate decisionDate;

  @Column(name = "decision_grounds")
  private String decisionGrounds;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  private List<DecisionNameDTO> decisionNames = new ArrayList<>();

  @Column(nullable = false, unique = true, updatable = false, name = "document_number")
  @NotBlank
  private String documentNumber;

  @ManyToOne
  @JoinColumn(name = "document_type_id")
  private DocumentTypeDTO documentType;

  @Column private String ecli;

  @OneToMany(
      mappedBy = "documentationUnit",
      cascade = CascadeType.ALL,
      fetch = FetchType.EAGER,
      orphanRemoval = true)
  @Builder.Default
  private List<FileNumberDTO> fileNumbers = new ArrayList<>();

  @Column private String grounds;

  @Column(name = "guiding_principle")
  private String guidingPrinciple;

  @Column private String headline;

  @Column private String headnote;

  @Column(name = "input_type")
  private String inputType;

  @Column(name = "judicial_body")
  private String judicialBody;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id")
  @Builder.Default
  private List<NormReferenceDTO> normReferences = new ArrayList<>();

  @OneToOne(mappedBy = "documentationUnit", cascade = CascadeType.ALL, orphanRemoval = true)
  private OriginalFileDocumentDTO originalFileDocument;

  @Column(name = "other_long_text")
  String otherLongText;

  @Column(name = "other_headnote")
  String otherHeadnote;

  @ManyToMany(
      cascade = {CascadeType.MERGE, CascadeType.PERSIST}, // TODO more?
      fetch = FetchType.EAGER)
  @JoinTable(
      name = "procedure_link",
      schema = "public",
      joinColumns = @JoinColumn(name = "documentation_unit_id"),
      inverseJoinColumns = @JoinColumn(name = "procedure_id"))
  private List<ProcedureDTO> procedures;

  @ManyToMany(
      cascade = {CascadeType.MERGE},
      fetch = FetchType.EAGER)
  @JoinTable(
      name = "documentation_unit_region",
      schema = "incremental_migration",
      joinColumns = @JoinColumn(name = "documentation_unit_id"),
      inverseJoinColumns = @JoinColumn(name = "region_id"))
  @Builder.Default
  private List<RegionDTO> regions = new ArrayList<>();

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "documentation_unit_field_of_law",
      schema = "incremental_migration",
      joinColumns = @JoinColumn(name = "documentation_unit_id"),
      inverseJoinColumns = @JoinColumn(name = "field_of_law_id"))
  @Builder.Default
  private List<FieldOfLawDTO> fieldsOfLaw = new ArrayList<>();

  @Column private String source;

  @Column private String tenor;

  @Column(name = "legal_effect")
  @Enumerated(EnumType.STRING)
  private LegalEffectDTO legalEffect;

  @ManyToOne(optional = false)
  @NotNull
  @JoinColumn(name = "documentation_office_id", referencedColumnName = "id")
  private DocumentationOfficeDTO documentationOffice;

  @OneToMany(mappedBy = "documentationUnitDTO", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @OrderBy("createdAt desc")
  private List<StatusDTO> status;

  public void setOriginalFileDocument(OriginalFileDocumentDTO originalFileDocument) {
    if (originalFileDocument != null) {
      originalFileDocument.setDocumentationUnit(this);
    }
    this.originalFileDocument = originalFileDocument;
  }

  // Gericht
  @ManyToOne
  @JoinColumn(name = "court_id", referencedColumnName = "id")
  private CourtDTO court;

  // Aktivzitierung
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id")
  @Builder.Default
  private List<ActiveCitationDTO> activeCitations = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  private List<DeviatingDateDTO> deviatingDates = new ArrayList<>();

  //
  //  @OneToMany(
  //      mappedBy = "documentationUnit",
  //      cascade = CascadeType.ALL,
  //      fetch = FetchType.EAGER,
  //      orphanRemoval = true)
  //  @Builder.Default
  //  private Set<DeviatingDocumentNumber> deviatingDocumentNumbers = new HashSet<>();
  //
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  private List<DeviatingEcliDTO> deviatingEclis = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  private List<DeviatingFileNumberDTO> deviatingFileNumbers = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  private List<DeviatingCourtDTO> deviatingCourts = new ArrayList<>();

  @ManyToMany(
      cascade = {CascadeType.MERGE},
      fetch = FetchType.EAGER)
  @JoinTable(
      name = "documentation_unit_keyword",
      schema = "incremental_migration",
      joinColumns = @JoinColumn(name = "documentation_unit_id"),
      inverseJoinColumns = @JoinColumn(name = "keyword_id"))
  @Builder.Default
  private Set<KeywordDTO> keywords = new HashSet<>();

  // Nachgehende Entscheidungen
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id")
  @Builder.Default
  private List<EnsuingDecisionDTO> ensuingDecisions = new ArrayList<>();

  // Nachgehende Entscheidungen mit Prädikat anhängig
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id")
  @Builder.Default
  private List<PendingDecisionDTO> pendingDecisions = new ArrayList<>();

  // Vorgehende Entscheidungen
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id")
  @Builder.Default
  private List<PreviousDecisionDTO> previousDecisions = new ArrayList<>();
}
