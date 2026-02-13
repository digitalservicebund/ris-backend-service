package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "foreign_language_version")
public class ForeignLanguageVersionDTO {

  @Id @GeneratedValue private UUID id;

  @ManyToOne
  @JoinColumn(name = "language_code_id")
  private LanguageCodeDTO languageCode;

  @Column(name = "url", length = 2048)
  private String url;

  @Column @NotNull private Long rank;
}
