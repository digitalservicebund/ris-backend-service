package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.util.List;
import lombok.Builder;

/**
 * @param evsf Elektronische Vorschriftensammlung Bundesfinanzverwaltung
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
    List<String> collectiveAgreements,
    List<Definition> definitions,
    List<ForeignLanguageVersion> foreignLanguageVersions,
    boolean hasLegislativeMandate,
    String evsf) {}
