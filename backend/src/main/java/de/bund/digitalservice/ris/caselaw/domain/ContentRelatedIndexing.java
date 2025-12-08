package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.appeal.Appeal;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.util.List;
import lombok.Builder;

/**
 * DE: Inhaltliche Erschließung
 *
 * @param evsf Elektronische Vorschriftensammlung Bundesfinanzverwaltung
 * @param appealAdmission DE: Rechtsmittelzulassung
 * @param appeal DE: Rechtsmittel
 * @param countriesOfOrigin DE: Herkunftsland
 */
@Builder(toBuilder = true)
public record ContentRelatedIndexing(
    List<String> keywords,
    List<String> jobProfiles,
    List<FieldOfLaw> fieldsOfLaw,
    List<NormReference> norms,
    List<ActiveCitation> activeCitations,
    List<String> dismissalGrounds,
    List<String> dismissalTypes,
    List<CollectiveAgreement> collectiveAgreements,
    List<Definition> definitions,
    List<ForeignLanguageVersion> foreignLanguageVersions,
    List<OriginOfTranslation> originOfTranslations,
    List<ObjectValue> objectValues,
    List<AbuseFee> abuseFees,
    List<IncomeType> incomeTypes,
    List<NormReference> nonApplicationNorms,
    boolean hasLegislativeMandate,
    String evsf,
    AppealAdmission appealAdmission,
    Appeal appeal,
    List<CountryOfOrigin> countriesOfOrigin) {}
