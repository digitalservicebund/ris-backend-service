package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
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
@Builder(toBuilder = true)
@Entity
@Table(name = "numeric_figure")
public class NumericFigureDTO {

  @Id private UUID judicialBodyId;

  @Column(name = "from_value", nullable = false)
  @NotBlank
  private String fromValue;

  @Column(name = "to_value", nullable = false)
  @NotBlank
  private String toValue;

  @Column(nullable = false)
  @NotBlank
  private String type;

  @OneToOne
  @MapsId
  @JoinColumn(name = "judicial_body_id")
  private JudicialBodyDTO judicialBody;
}
