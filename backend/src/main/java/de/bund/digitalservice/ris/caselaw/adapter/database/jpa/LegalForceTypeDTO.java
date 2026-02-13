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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** An interface representing the type of a legal force. */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "legal_force_type")
public class LegalForceTypeDTO {
  @Id @GeneratedValue private UUID id;

  @Column
  @Size(max = 255)
  @NotBlank
  private String abbreviation;

  @Column
  @Size(max = 255)
  @NotBlank
  private String label;

  @Column(name = "juris_id")
  @ToString.Include
  @NotNull
  private Integer jurisId;
}
