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
import org.hibernate.annotations.UuidGenerator;

/** DE: Herkunftsland */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(schema = "incremental_migration", name = "country_of_origin")
public class CountryOfOriginDto {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(updatable = false, nullable = false)
  private UUID id;

  @Column(name = "value")
  @Size(max = 255)
  private String legacyValue;

  /** DE: Landbezeichnung */
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "country_id")
  private FieldOfLawDTO country;

  /** DE: Rechtlicher Rahmen */
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "field_of_law_id")
  private FieldOfLawDTO fieldOfLaw;

  @Column @NotNull private Long rank;
}
