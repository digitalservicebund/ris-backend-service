package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(schema = "incremental_migration", name = "foreign_language_version")
public class ForeignLanguageVersionDTO {

  @Id @GeneratedValue private UUID id;

  @ManyToOne
  @JoinColumn(name = "language_code_id", nullable = false)
  private LanguageCodeDTO languageCode;

  @Column(name = "url", length = 2048)
  private String url;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  private DecisionDTO documentationUnit;

  @Column @NotNull private Long rank;
}
