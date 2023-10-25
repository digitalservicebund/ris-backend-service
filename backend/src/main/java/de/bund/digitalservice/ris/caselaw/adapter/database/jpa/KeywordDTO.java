package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Table(schema = "incremental_migration", name = "keyword")
public class KeywordDTO {
  @Id private UUID id;

  @Column(nullable = false)
  @NotBlank
  private String value;

  private Long rank;

  public KeywordDTO(String value) {
    this.value = value;
  }
}
