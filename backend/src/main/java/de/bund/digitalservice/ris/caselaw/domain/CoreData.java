package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.validator.LookupTableConstraint;
import java.time.Instant;
import java.util.List;
import javax.validation.constraints.PastOrPresent;
import lombok.Builder;

@Builder
public record CoreData(
    List<String> fileNumbers,
    List<String> deviatingFileNumbers,
    Court court,
    DocumentType documentType,
    String procedure,
    String ecli,
    List<String> deviatingEclis,
    String appraisalBody,
    @PastOrPresent Instant decisionDate,
    List<Instant> deviatingDecisionDates,
    @LookupTableConstraint(lookupTableName = "legalEffect") String legalEffect,
    String inputType,
    String center,
    String region) {}
