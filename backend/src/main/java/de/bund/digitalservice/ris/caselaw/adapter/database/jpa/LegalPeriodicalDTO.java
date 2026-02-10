package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "legal_periodical")
@Getter
@Setter
public class LegalPeriodicalDTO {
  @Id @GeneratedValue private UUID id;

  @NotBlank private String abbreviation;

  @Column private String title;

  @Column private String subtitle;

  @Column(name = "primary_reference")
  private Boolean primaryReference;

  @Column(name = "citation_style")
  private String citationStyle;

  @Column(name = "juris_id")
  private int jurisId;
}
