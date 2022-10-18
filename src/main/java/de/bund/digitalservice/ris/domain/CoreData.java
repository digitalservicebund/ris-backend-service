package de.bund.digitalservice.ris.domain;

import de.bund.digitalservice.ris.domain.validator.LookupTableConstraint;
import java.time.Instant;
import javax.validation.constraints.PastOrPresent;
import lombok.Builder;

@Builder
public record CoreData(
    String fileNumber,
    String courtType,
    String category,
    String procedure,
    String ecli,
    String appraisalBody,
    @PastOrPresent Instant decisionDate,
    String courtLocation,
    @LookupTableConstraint(lookupTableName = "legalEffect") String legalEffect,
    String inputType,
    String center,
    String region) {}
