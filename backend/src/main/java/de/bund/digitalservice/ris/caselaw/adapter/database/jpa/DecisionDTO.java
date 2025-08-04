package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.proxy.HibernateProxy;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "decision", schema = "incremental_migration")
@SuppressWarnings(
    "java:S6539") // This class depends on many classes, because it's the key part and merging
// everything.
public class DecisionDTO extends DocumentationUnitDTO {

  /** Tatbestand */
  @Column(name = "case_facts")
  private String caseFacts;

  /** Entscheidungsgründe */
  @Column(name = "decision_grounds")
  private String decisionGrounds;

  /** Entscheidungsname */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  private List<DecisionNameDTO> decisionNames = new ArrayList<>();

  @Column(name = "celex")
  private String celexNumber;

  @Column private String ecli;

  /** Gründe */
  @Column private String grounds;

  /** Leitsatz */
  @Column(name = "guiding_principle")
  private String guidingPrinciple;

  /** Orientierungssatz */
  @Column private String headnote;

  /** Eingangsart */
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  @OrderBy("rank")
  private List<InputTypeDTO> inputTypes = new ArrayList<>();

  /** Sonstiger Langtext */
  @Column(name = "other_long_text")
  String otherLongText;

  /** Gliederung */
  @Column(name = "outline")
  String outline;

  /** Sonstiger Orientierungssatz */
  @Column(name = "other_headnote")
  String otherHeadnote;

  /** Abweichende Meinung */
  @Column(name = "dissenting_opinion")
  private String dissentingOpinion;

  @OneToMany
  @JoinTable(
      name = "documentation_unit_procedure",
      schema = "incremental_migration",
      joinColumns = @JoinColumn(name = "documentation_unit_id"),
      inverseJoinColumns = @JoinColumn(name = "procedure_id"))
  @OrderColumn(name = "rank")
  @Builder.Default
  private List<ProcedureDTO> procedureHistory = new ArrayList<>();

  /** Vorgang */
  @OneToOne()
  @JoinColumn(name = "current_procedure_id")
  private ProcedureDTO procedure;

  /** Quelle */
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  @OrderBy("rank")
  private List<SourceDTO> source = new ArrayList<>();

  @Column private String tenor;

  /** Elektronische Vorschriftensammlung Bundesfinanzverwaltung */
  @Column private String evsf;

  /** Rechtskraft */
  @Column(name = "legal_effect")
  @Enumerated(EnumType.STRING)
  private LegalEffectDTO legalEffect;

  @ManyToOne()
  @JoinColumn(name = "creating_documentation_office_id", referencedColumnName = "id")
  private DocumentationOfficeDTO creatingDocumentationOffice;

  /** Aktivzitierung */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  @OrderBy("rank")
  private List<ActiveCitationDTO> activeCitations = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  @OrderBy("rank")
  private List<DeviatingEcliDTO> deviatingEclis = new ArrayList<>();

  /** Nachgehende Entscheidungen */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  @OrderBy("rank")
  private List<EnsuingDecisionDTO> ensuingDecisions = new ArrayList<>();

  /** Nachgehende Entscheidungen mit Prädikat anhängig */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  @OrderBy("rank")
  private List<PendingDecisionDTO> pendingDecisions = new ArrayList<>();

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  @OrderBy("rank")
  private List<LeadingDecisionNormReferenceDTO> leadingDecisionNormReferences = new ArrayList<>();

  @Column private String note;

  /** Streitjahr */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @Builder.Default
  @OrderBy("rank")
  private Set<YearOfDisputeDTO> yearsOfDispute = new HashSet<>();

  /** Berufsbild */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @OrderBy("rank")
  @Builder.Default
  private List<JobProfileDTO> jobProfiles = new ArrayList<>();

  /** Kündigungsgründe */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @OrderBy("rank")
  @Builder.Default
  private List<DismissalGroundsDTO> dismissalGrounds = new ArrayList<>();

  /** Kündigungsarten */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @OrderBy("rank")
  @Builder.Default
  private List<DismissalTypesDTO> dismissalTypes = new ArrayList<>();

  /** Tarifvertrag */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @OrderBy("rank")
  @Builder.Default
  private List<CollectiveAgreementDTO> collectiveAgreements = new ArrayList<>();

  /** Gesetzgebungsauftrag */
  @Column(name = "legislative_mandate")
  private boolean hasLegislativeMandate;

  /** Mitwirkende Richter */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @OrderBy("rank")
  @Builder.Default
  private List<ParticipatingJudgeDTO> participatingJudges = new ArrayList<>();

  /* Definition */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @OrderBy("rank")
  @Builder.Default
  private List<DefinitionDTO> definitions = new ArrayList<>();

  @OneToMany(mappedBy = "documentationUnit1", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JsonManagedReference // Prevent infinite recursion
  @Builder.Default
  private Set<DuplicateRelationDTO> duplicateRelations1 = new HashSet<>();

  @OneToMany(mappedBy = "documentationUnit2", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JsonManagedReference // Prevent infinite recursion
  @Builder.Default
  private Set<DuplicateRelationDTO> duplicateRelations2 = new HashSet<>();

  // Fremdsprachige Fassung
  @OneToMany(
      mappedBy = "documentationUnit",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @OrderBy("rank")
  @Builder.Default
  private List<ForeignLanguageVersionDTO> foreignLanguageVersions = new ArrayList<>();

  @Override
  @SuppressWarnings("java:S2097") // Class type check is not recognized by Sonar
  public final boolean equals(Object o) {
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
    DecisionDTO that = (DecisionDTO) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy hibernateProxy
        ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
