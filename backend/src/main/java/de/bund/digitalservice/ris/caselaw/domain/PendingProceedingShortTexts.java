package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

/**
 * The domain model for pending proceeding short texts section
 *
 * @param headline the headline (Titelzeile)
 * @param resolutionNote the resolution note (Erledigungsvermerk)
 * @param legalIssue the legal issue (Rechtsfrage)
 * @param admissionOfAppeal the admission of appeal (Rechtsmittelzulassung)
 * @param appellant the appellant (Rechtsmittelführer)
 */
@Builder(toBuilder = true)
public record PendingProceedingShortTexts(
    String headline,
    String resolutionNote,
    String legalIssue,
    String admissionOfAppeal,
    String appellant) {}
