package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "judicial_body")
public class JudicialBodyDTO {

  @Id @GeneratedValue private UUID id;

  @OneToOne(
      mappedBy = "judicialBody",
      cascade = CascadeType.ALL,
      fetch = FetchType.EAGER,
      orphanRemoval = true)
  @PrimaryKeyJoinColumn
  private NumericFigureDTO numericFigure;

  @Column(nullable = false)
  @NotBlank
  private String name;

  @ManyToOne @NotNull private CourtDTO court;
}
