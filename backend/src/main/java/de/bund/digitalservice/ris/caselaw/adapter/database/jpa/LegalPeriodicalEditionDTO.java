package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "edition", schema = "incremental_migration")
@Getter
@Setter
public class LegalPeriodicalEditionDTO {
  @Id private UUID id;

  @NotNull
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "legal_periodical_id")
  private LegalPeriodicalDTO legalPeriodical;

  @Column private String name;

  @Column private String prefix;

  @Column private String suffix;

  @CreatedDate
  @Column(name = "created_at")
  private LocalDate createdAt;

  @Builder.Default
  @OneToMany(mappedBy = "edition", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<EditionReferenceDTO> editionReferences = new ArrayList<>();

  // Methods to get references and literature citations
  public List<UUID> getLiteratureCitations() {
    return editionReferences.stream()
        .filter(ref -> "literature".equals(ref.getDtype()))
        .map(EditionReferenceDTO::getReferenceId)
        .collect(Collectors.toList());
  }

  public List<UUID> getReferences() {
    return editionReferences.stream()
        .filter(ref -> "reference".equals(ref.getDtype()))
        .map(EditionReferenceDTO::getReferenceId)
        .collect(Collectors.toList());
  }

  public void setLiteratureCitations(List<DependentLiteratureCitationDTO> literatureCitations) {
    // Remove existing literature citations
    editionReferences.removeIf(ref -> "literature".equals(ref.getDtype()));

    // Add new literature citations with updated rank
    for (DependentLiteratureCitationDTO citation : literatureCitations) {
      EditionReferenceDTO editionReference = new EditionReferenceDTO();
      editionReference.setEdition(this);
      editionReference.setReferenceId(citation.getId());
      editionReference.setRank(citation.getRank());
      editionReference.setDtype("literature");
      editionReferences.add(editionReference);
    }
  }

  public void setReferences(List<ReferenceDTO> references) {
    // Remove existing references
    editionReferences.removeIf(ref -> "reference".equals(ref.getDtype()));

    // Add new references with updated rank
    for (ReferenceDTO reference : references) {
      EditionReferenceDTO editionReference = new EditionReferenceDTO();
      editionReference.setReferenceId(reference.getId());
      editionReference.setRank(reference.getRank());
      editionReference.setDtype("reference");
      editionReference.setEdition(this);
      editionReferences.add(editionReference);
    }
  }
}
