package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.validator.DateKnownConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@NoArgsConstructor
@DateKnownConstraint
public class ActiveCitation extends LinkedDocumentationUnit {}
