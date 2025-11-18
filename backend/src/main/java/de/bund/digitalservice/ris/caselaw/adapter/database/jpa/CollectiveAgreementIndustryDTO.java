package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

/** Possible values for the industry (DE: branche) of a collective agrement (DE: Tarifvertrag). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Immutable
@Table(schema = "incremental_migration", name = "collective_agreement_industry")
public class CollectiveAgreementIndustryDTO {

  @Id @GeneratedValue private UUID id;

  @Column
  @Size(max = 255)
  @NotBlank
  private String value;
}
