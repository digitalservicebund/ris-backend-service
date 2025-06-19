package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;
import java.util.UUID;

/**
 * The shared domain model interface for a caselaw decision {@link Decision} and a pending
 * proceeding {@link PendingProceeding}.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes({
  @JsonSubTypes.Type(value = Decision.class, name = "DOCUMENTION_UNIT"),
  @JsonSubTypes.Type(value = PendingProceeding.class, name = "PENDING_PROCEEDING")
})
public interface DocumentationUnit {

  UUID uuid();

  Long version();

  String documentNumber();

  CoreData coreData();

  List<PreviousDecision> previousDecisions();

  Status status();

  ContentRelatedIndexing contentRelatedIndexing();

  List<Reference> caselawReferences();

  List<Reference> literatureReferences();

  ManagementData managementData();

  List<String> documentalists();

  boolean isEditable();

  boolean isDeletable();

  Kind kind();
}
