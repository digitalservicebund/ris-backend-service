package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
@Builder
@Entity
@Table(name = "field_of_law_norm")
public class FieldOfLawNormDTO {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false)
  @NotBlank
  private String abbreviation;

  @Column(name = "single_norm_description")
  private String singleNormDescription;

  @ManyToOne
  @JoinColumn(name = "field_of_law_id")
  private FieldOfLawDTO fieldOfLaw;
}
