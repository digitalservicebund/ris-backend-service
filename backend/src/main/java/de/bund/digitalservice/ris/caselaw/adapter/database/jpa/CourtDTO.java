package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
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
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "court", schema = "incremental_migration")
public class CourtDTO {
  @Id @GeneratedValue private UUID id;

  @Column @NotBlank private String type;

  @Column private String location;

  @Column private String field;

  @Column(name = "is_superior_court")
  @NotNull
  @Accessors(fluent = true)
  private Boolean isSuperiorCourt;

  @Column(name = "is_foreign_court")
  @NotNull
  @Accessors(fluent = true)
  private Boolean isForeignCourt;

  @ManyToOne
  @JoinTable(
      name = "court_region",
      joinColumns = @JoinColumn(name = "court_id"),
      inverseJoinColumns = @JoinColumn(name = "region_id"),
      schema = "incremental_migration")
  private RegionDTO region;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @Valid
  private AddressDTO address;

  @Column(name = "belongs_to")
  private String belongsTo;

  @Column(name = "late_name")
  private String lateName;

  @Column(name = "official_name")
  private String officialName;

  @Column(name = "early_name")
  private String earlyName;

  @Column(name = "traditional_name")
  private String traditionalName;

  @Column(name = "current_branch")
  private String currentBranch;

  @Column(name = "deprecated_branch")
  private String deprecatedBranch;

  @Column(name = "belongs_to_branch")
  private String belongsToBranch;

  @Column(name = "additional_information")
  private String additionalInformation;

  @Column(name = "exists_since")
  private LocalDate existsSince;

  @Column(name = "deprecated_since")
  private LocalDate deprecatedSince;

  @Column(name = "can_deliver_lrs")
  @Accessors(fluent = true)
  private Boolean canDeliverLrs;

  @Column private String remark;

  @OneToMany(
      mappedBy = "court",
      cascade = CascadeType.ALL,
      fetch = FetchType.EAGER,
      orphanRemoval = true)
  @Builder.Default
  @Valid
  private Set<JudicialBodyDTO> judicialBodies = new HashSet<>();

  @OneToMany(
      mappedBy = "court",
      cascade = CascadeType.ALL,
      fetch = FetchType.EAGER,
      orphanRemoval = true)
  @Builder.Default
  @Valid
  private Set<SynonymDTO> synonyms = new HashSet<>();

  @Column(name = "juris_id", nullable = false, unique = true, updatable = false)
  @ToString.Include
  @NotNull
  private Integer jurisId;

  public void setJudicialBodies(Set<JudicialBodyDTO> entities) {
    entities.forEach(e -> e.setCourt(this));
    this.judicialBodies = new HashSet<>();
    this.judicialBodies.addAll(entities);
  }

  public void setSynonyms(Set<SynonymDTO> entities) {
    entities.forEach(e -> e.setCourt(this));
    this.synonyms = new HashSet<>();
    this.synonyms.addAll(entities);
  }
}
