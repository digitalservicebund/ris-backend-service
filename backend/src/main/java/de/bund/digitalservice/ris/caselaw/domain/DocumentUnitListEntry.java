package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

/**
 * A record representing the domain object of a documentation unit, only show relevant information
 * to be shown in a list view
 *
 * @param uuid the identifier of the search result
 * @param documentNumber (Dokumentnummer) of the documentation unit list entry
 * @param decisionDate (Entscheidungsdatum) of the documentation unit list entry
 * @param fileNumber (Aktenzeichen) of the documentation unit list entry
 * @param documentType (Dokumenttyp) of the documentation unit list entry
 * @param court (Gericht) of the documentation unit list entry
 * @param appraisalBody (Spruchkörper) of the documentation unit list entry
 * @param documentationOffice (Dokumentationsstelle) of the documentation unit list entry
 * @param status (Status) of the documentation unit list entry
 */
@Builder
public record DocumentUnitListEntry(
    UUID uuid,
    String documentNumber,
    LocalDate decisionDate,
    String fileNumber,
    DocumentType documentType,
    Court court,
    String appraisalBody,
    DocumentationOffice documentationOffice,
    Status status) {}
