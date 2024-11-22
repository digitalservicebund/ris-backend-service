package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "edition_reference", schema = "incremental_migration")
public class EditionReferenceDTO {
  @Id @GeneratedValue private UUID id;

  @Column(name = "rank")
  private Integer rank;

  @Column(name = "dtype")
  private String dtype;

  @ManyToOne
  @JoinColumn(name = "edition_id", nullable = false)
  private LegalPeriodicalEditionDTO edition;

  // A single reference field to hold either a DependentLiteratureCitationDTO or ReferenceDTO
  @Transient private Object reference;

  // Relationships with insertable=false, updatable=false to avoid column duplication
  @ManyToOne
  @JoinColumn(name = "reference_id", insertable = false, updatable = false)
  private DependentLiteratureCitationDTO literatureReference;

  @ManyToOne
  @JoinColumn(name = "reference_id", insertable = false, updatable = false)
  private ReferenceDTO caselawReference;

  public void setDtype(String dtype) {
    this.dtype = dtype;
    // Update the transient reference field based on the dtype
    if ("literature".equals(dtype)) {
      this.reference = literatureReference;
    } else if ("reference".equals(dtype)) {
      this.reference = caselawReference;
    }
  }

  // Method to get the resolved reference
  public Object getReference() {
    if ("literature".equals(dtype)) {
      return literatureReference;
    } else if ("reference".equals(dtype)) {
      return caselawReference;
    }
    return null;
  }

  public void setReference(Object reference) {
    if (reference instanceof DependentLiteratureCitationDTO) {
      this.literatureReference = (DependentLiteratureCitationDTO) reference;
      this.dtype = "literature";
    } else if (reference instanceof ReferenceDTO) {
      this.caselawReference = (ReferenceDTO) reference;
      this.dtype = "reference";
    }
    this.reference = reference;
  }
}
