package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationType.Values;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@Getter
@Entity
@DiscriminatorValue(Values.ENSUING_DECISION)
public class EnsuingDecisionDTO extends RelatedDocumentationDTO {

  @Column private String note;

  @Override
  public boolean equals(Object other) {
    return super.equals(other);
  }
}
