package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;

/**
 * The domain model interface for a DocumentationUnit, a caselaw decision or a pending proceeding
 */
public interface Documentable {

  public UUID uuid();

  public String documentNumber();

  public CoreData coreData();

  public List<PreviousDecision> previousDecisions();

  public List<EnsuingDecision> ensuingDecisions();

  public ShortTexts shortTexts();

  public Status status();

  public ContentRelatedIndexing contentRelatedIndexing();

  public List<Reference> caselawReferences();

  public List<Reference> literatureReferences();

  public boolean isEditable();

  public boolean isDeletable();
}
