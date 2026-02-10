package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "year_of_dispute")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class YearOfDisputeDTO {

  @Id @GeneratedValue private UUID id;

  @Column @NotNull private Integer rank;

  @Column(nullable = false)
  @Size(min = 4, max = 4)
  @NotBlank
  @EqualsAndHashCode.Include
  private String value;
}
