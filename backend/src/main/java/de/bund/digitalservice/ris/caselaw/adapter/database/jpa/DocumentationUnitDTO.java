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
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashSet;
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
public class DocumentationUnitDTO {

  @Id @GeneratedValue private UUID id;

  @Column(name = "case_facts")
  private String caseFacts;

  @Column(name = "decision_date")
  private LocalDate decisionDate;

  @Column(name = "decision_grounds")
  private String decisionGrounds;

  @OneToMany(
      mappedBy = "documentationUnit",
      cascade = CascadeType.ALL,
      fetch = FetchType.EAGER,
      orphanRemoval = true)
  @Builder.Default
  private Set<DecisionNameDTO> decisionNames = new HashSet<>();

  @Column(nullable = false, unique = true, updatable = false, name = "document_number")
  @NotBlank
  private String documentNumber;

  @ManyToOne(optional = false)
  @JoinColumn(name = "document_type_id")
  // TODO  @NotNull
  private DocumentTypeDTO documentType;

  @Column private String ecli;

  @OneToMany(
      mappedBy = "documentationUnit",
      cascade = CascadeType.ALL,
      fetch = FetchType.EAGER,
      orphanRemoval = true)
  @Builder.Default
  private Set<FileNumberDTO> fileNumbers = new HashSet<>();

  @Column private String grounds;

  @Column(name = "guiding_principle")
  private String guidingPrinciple;

  @Column private String headline;

  @Column private String headnote;

  @Column(name = "input_type")
  private String inputType;

  @Column(name = "judicial_body")
  private String judicialBody;

  @OneToMany(
      mappedBy = "documentationUnit",
      cascade = CascadeType.ALL,
      fetch = FetchType.EAGER,
      orphanRemoval = true)
  @Builder.Default
  private Set<NormReferenceDTO> normReferences = new HashSet<>();

  @OneToOne(mappedBy = "documentationUnit", cascade = CascadeType.ALL, orphanRemoval = true)
  @PrimaryKeyJoinColumn
  private OriginalFileDocumentDTO originalFileDocument;

  @Column(name = "other_long_text")
  String otherLongText;

  @Column(name = "other_headnote")
  String otherHeadnote;

  @Column private String procedure;

  @ManyToMany(
      cascade = {CascadeType.MERGE},
      fetch = FetchType.EAGER)
  @JoinTable(
      name = "documentation_unit_region",
      schema = "incremental_migration",
      joinColumns = @JoinColumn(name = "documentation_unit_id"),
      inverseJoinColumns = @JoinColumn(name = "region_id"))
  @Builder.Default
  private Set<RegionDTO> regions = new HashSet<>();

  @Column private String source;

  @Column private String tenor;

  @Column(name = "legal_effect")
  @Enumerated(EnumType.STRING)
  private LegalEffectDTO legalEffect;

  @ManyToOne(optional = false)
  @NotNull
  @JoinColumn(name = "documentation_office_id", referencedColumnName = "id")
  private DocumentationOfficeDTO documentationOffice;

  public void setOriginalFileDocument(OriginalFileDocumentDTO originalFileDocument) {
    if (originalFileDocument != null) {
      originalFileDocument.setDocumentationUnit(this);
    }
    this.originalFileDocument = originalFileDocument;
  }

  // Other NeuRIS Categories:

  // Gericht
  @ManyToOne
  @JoinColumn(name = "court_id", referencedColumnName = "id")
  private CourtDTO court;

  // Aktivzitierung
  //  @OneToMany(
  //      mappedBy = "documentationUnit",
  //      cascade = CascadeType.ALL,
  //      fetch = FetchType.EAGER,
  //      orphanRemoval = true)
  // @Builder.Default
  // private Set<CaselawActiveCitation> caselawActiveCitations = new HashSet<>();

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id")
  @Builder.Default
  private Set<DeviatingDateDTO> deviatingDates = new HashSet<>();

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
  @JoinColumn(name = "documentation_unit_id")
  @Builder.Default
  private Set<DeviatingEcliDTO> deviatingEclis = new HashSet<>();

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id")
  @Builder.Default
  private Set<DeviatingFileNumberDTO> deviatingFileNumbers = new HashSet<>();

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id")
  @Builder.Default
  private Set<DeviatingCourtDTO> deviatingCourts = new HashSet<>();

  //
  //    @ManyToOne(optional = false)
  //    @NotNull
  //    private DocumentationOffice documentationOffice;

  // Nachgehende Entscheidungen
  //  @OneToMany(
  //      mappedBy = "documentationUnit",
  //      cascade = CascadeType.ALL,
  //      fetch = FetchType.EAGER,
  //      orphanRemoval = true)
  //  @Builder.Default
  //  private Set<EnsuingDecision> ensuingDecisions = new HashSet<>();

  @ManyToMany(cascade = {CascadeType.MERGE})
  @JoinTable(
      name = "documentation_unit_keyword",
      joinColumns = @JoinColumn(name = "documentation_unit_id"),
      inverseJoinColumns = @JoinColumn(name = "keyword_id"))
  @Builder.Default
  private Set<KeywordDTO> keywords = new HashSet<>();

  // Nachgehende Entscheidungen mit Prädikat anhängig
  //  @OneToMany(
  //      mappedBy = "documentationUnit",
  //      cascade = CascadeType.ALL,
  //      fetch = FetchType.EAGER,
  //      orphanRemoval = true)
  //  @Builder.Default
  //  private Set<PendingDecision> pendingDecisions = new HashSet<>();

  // Nachgehende Entscheidungen
  //  @OneToMany(
  //      mappedBy = "documentationUnit",
  //      cascade = CascadeType.ALL,
  //      fetch = FetchType.EAGER,
  //      orphanRemoval = true)
  //  @Builder.Default
  //  private Set<PreviousDecision> previousDecisions = new HashSet<>();

}
