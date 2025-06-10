package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;
import java.util.UUID;

/**
 * The shared domain model interface for a caselaw decision {@link
 * de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit} and a pending proceeding {@link
 * de.bund.digitalservice.ris.caselaw.domain.PendingProceeding}.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DocumentationUnit.class, name = "DOCUMENT_UNIT"),
  @JsonSubTypes.Type(value = PendingProceeding.class, name = "PENDING_PROCEEDING")
})
public interface Documentable {

  UUID uuid();

  Long version();

  String documentNumber();

  CoreData coreData();

  List<PreviousDecision> previousDecisions();

  List<EnsuingDecision> ensuingDecisions();

  ShortTexts shortTexts();

  Status status();

  ContentRelatedIndexing contentRelatedIndexing();

  List<Reference> caselawReferences();

  List<Reference> literatureReferences();

  List<String> documentalists();

  boolean isEditable();

  boolean isDeletable();

  Kind kind();
}
