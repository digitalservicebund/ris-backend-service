package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.validator.LookupTableConstraint;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record CoreData(
    List<String> fileNumbers,
    List<String> deviatingFileNumbers,
    Court court,
    List<String> incorrectCourts,
    DocumentType documentType,
    Procedure procedure,
    List<String> previousProcedures,
    String ecli,
    List<String> deviatingEclis,
    String appraisalBody,
    @PastOrPresent LocalDate decisionDate,
    boolean dateKnown,
    List<LocalDate> deviatingDecisionDates,
    @LookupTableConstraint(lookupTableName = "legalEffect") String legalEffect,
    String inputType,
    DocumentationOffice documentationOffice,
    String region) {}
