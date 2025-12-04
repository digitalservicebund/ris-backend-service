package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.NormReferenceType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** An interface representing a norm reference, linked to a documentation unit. */
@NoArgsConstructor
@SuperBuilder
@Getter
@Entity
@DiscriminatorValue(NormReferenceType.NORM)
public class NormReferenceDTO extends AbstractNormReferenceDTO {}
