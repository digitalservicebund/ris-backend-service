package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
@Entity()
@Table(name = "norm_abbreviation")
public class NormAbbreviationDTO {

  @Id @GeneratedValue private UUID id;

  @Column
  @Size(max = 255)
  @NotBlank
  private String abbreviation;

  @Column(name = "decision_date")
  private LocalDate decisionDate;

  @Column(nullable = false, unique = true, name = "document_id")
  @NotNull
  private Long documentId;

  @Column(name = "document_number")
  private String documentNumber;

  @OneToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "norm_abbreviation_document_type",
      joinColumns = @JoinColumn(name = "norm_abbreviation_id"),
      inverseJoinColumns = @JoinColumn(name = "document_type_id"))
  @Builder.Default
  private List<DocumentTypeDTO> documentTypeList = new ArrayList<>();

  @Column(name = "official_letter_abbreviation")
  private String officialLetterAbbreviation;

  @Column(name = "official_long_title")
  private String officialLongTitle;

  @Column(name = "official_short_title")
  private String officialShortTitle;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinTable(
      name = "norm_abbreviation_region",
      joinColumns = @JoinColumn(name = "norm_abbreviation_id"),
      inverseJoinColumns = @JoinColumn(name = "region_id"))
  private RegionDTO region;

  @Column
  @Size(min = 1, max = 1)
  private String source;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NormAbbreviationDTO that = (NormAbbreviationDTO) o;

    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
