package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
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
@Table(name = "edition", schema = "incremental_migration")
@Getter
@Setter
public class LegalPeriodicalEditionDTO {
  @Id @GeneratedValue private UUID id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "legal_periodical_id")
  private LegalPeriodicalDTO legalPeriodical;

  @Column @NotBlank private String name;

  @Column private String prefix;

  @Column private String suffix;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "edition_id")
  @Builder.Default
  @OrderBy("rank")
  private List<ReferenceDTO> references = new ArrayList<>();
}
