package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationType.Values;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@Entity
@DiscriminatorValue(Values.PREVIOUS_DECISION)
public class PreviousDecisionDTO extends RelatedDocumentationDTO {
  // Todo column missing
  // private boolean dateKnown;
}
