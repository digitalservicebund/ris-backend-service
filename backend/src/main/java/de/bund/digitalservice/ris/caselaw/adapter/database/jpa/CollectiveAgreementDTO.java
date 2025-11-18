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
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DE: Tarifvertrag */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(schema = "incremental_migration", name = "collective_agreement")
public class CollectiveAgreementDTO {

  @Id @GeneratedValue private UUID id;

  /** DE: Bezeichnung des Tarifvertrags */
  @Column
  @Size(max = 255)
  private String name;

  /** DE: Tarifnorm */
  @Column
  @Size(max = 255)
  private String norm;

  /** Format: DD.MM.YYYY or MM.YYYY or YYYY */
  @Column
  @Size(max = 10)
  private String date;

  /** DE: Branche */
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "industry_id")
  private CollectiveAgreementIndustryDTO industry;

  @Column @NotNull private Long rank;
}
