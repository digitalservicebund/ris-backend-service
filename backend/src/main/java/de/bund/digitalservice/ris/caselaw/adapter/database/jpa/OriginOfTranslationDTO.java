package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.TranslationType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(schema = "incremental_migration", name = "origin_of_translation")
public class OriginOfTranslationDTO {
  @Id @GeneratedValue private UUID id;

  @Column @NotNull private Long rank;

  @ManyToOne
  @JoinColumn(name = "language_code_id")
  private LanguageCodeDTO languageCode;

  @Column(name = "translation_type")
  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  private TranslationType translationType;

  @OneToMany(
      mappedBy = "originOfTranslation",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @OrderBy("rank")
  private List<OriginOfTranslationTranslatorDTO> translators;

  @OneToMany(
      mappedBy = "originOfTranslation",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @OrderBy("rank")
  private List<OriginOfTranslationBorderNumberDTO> borderNumbers;

  @OneToMany(
      mappedBy = "originOfTranslation",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @OrderBy("rank")
  private List<OriginOfTranslationUrlDTO> urls;
}
