package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.NormReferenceType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** A DTO representing a non-application norm (Nichtanwendungsgesetz) */
@NoArgsConstructor
@SuperBuilder
@Getter
@Entity
@DiscriminatorValue(NormReferenceType.NON_APPLICATION_NORM)
public class NonApplicationNormDTO extends AbstractNormReferenceDTO {}
