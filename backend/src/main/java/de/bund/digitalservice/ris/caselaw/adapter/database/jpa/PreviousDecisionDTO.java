package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@Entity
@DiscriminatorValue("previous_decision")
public class PreviousDecisionDTO extends RelatedDocumentationDTO {
  // Todo column missing
  //    private boolean dateKnown;
}
