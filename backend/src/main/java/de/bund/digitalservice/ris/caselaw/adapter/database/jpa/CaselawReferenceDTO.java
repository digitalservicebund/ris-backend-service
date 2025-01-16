package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
@Entity
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("caselaw")
public class CaselawReferenceDTO extends ReferenceDTO {

  @Column private String type;

  @Column(name = "reference_supplement")
  private String referenceSupplement;

  @Column private String footnote;
}
