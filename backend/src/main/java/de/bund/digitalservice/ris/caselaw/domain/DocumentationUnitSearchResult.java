package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

/**
 * A record representing the domain object of a minimized documentation unit returned by search,
 * that is suitable to be consumed by clients of the REST service.
 *
 * @param uuid the identifier of the search result
 * @param documentNumber (Dokumentnummer) of the search result
 * @param court (Gericht) of the search result
 * @param fileNumber (Aktenzeichen) of the search result
 * @param fileName (Dokumentname) of the search result
 * @param decisionDate (Entscheidungsdatum) of the search result
 * @param appraisalBody (Spruchkörper) of the search result
 * @param documentType (Dokumenttyp) of the search result
 * @param referencedDocumentationUnitId the id of a referenced documentation units, if search result
 *     from a search for related documentation units
 * @param status (Status) of the search result
 */
@Builder
public record DocumentationUnitSearchResult(
    UUID uuid,
    String documentNumber,
    Court court,
    String fileNumber,
    String fileName,
    LocalDate decisionDate,
    String appraisalBody,
    DocumentType documentType,
    UUID referencedDocumentationUnitId,
    Status status) {}
