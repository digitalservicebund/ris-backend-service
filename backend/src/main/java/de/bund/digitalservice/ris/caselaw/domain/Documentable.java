package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;

/**
 * The shared domain model interface for a caselaw decision {@link
 * de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit} and a pending proceeding {@link
 * de.bund.digitalservice.ris.caselaw.domain.PendingProceeding}.
 */
public interface Documentable {

  UUID uuid();

  String documentNumber();

  CoreData coreData();

  List<PreviousDecision> previousDecisions();

  List<EnsuingDecision> ensuingDecisions();

  ShortTexts shortTexts();

  Status status();

  ContentRelatedIndexing contentRelatedIndexing();

  List<Reference> caselawReferences();

  List<Reference> literatureReferences();

  boolean isEditable();

  boolean isDeletable();
}
