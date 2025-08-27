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
  @JsonSubTypes.Type(value = Decision.class, name = "DECISION"),
  @JsonSubTypes.Type(value = PendingProceeding.class, name = "PENDING_PROCEEDING")
})
public interface DocumentationUnit {

  UUID uuid();

  Long version();

  String documentNumber();

  CoreData coreData();

  List<PreviousDecision> previousDecisions();

  Status status();

  PortalPublicationStatus portalPublicationStatus();

  ContentRelatedIndexing contentRelatedIndexing();

  List<Reference> caselawReferences();

  List<Reference> literatureReferences();

  ManagementData managementData();

  List<String> documentalists();

  DocumentationUnitProcessStep currentDocumentationUnitProcessStep();

  ProcessStep previousProcessStep();

  List<DocumentationUnitProcessStep> processSteps();

  boolean isEditable();

  boolean isDeletable();

  Kind kind();
}
