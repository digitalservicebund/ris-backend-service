package de.bund.digitalservice.ris.caselaw.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/**
 * The domain model for a DocumentationUnit, a caselaw decision
 *
 * @param uuid the unique identifier
 * @param version the version
 * @param documentNumber the document number, pattern depending on the documentation office, e.g.
 *     KORE00012024
 * @param attachments a list of attachments (e.g. .docx files)
 * @param coreData the core data (e.g. file number, decision date)
 * @param previousDecisions a list of previous decisions (Vorinstanz)
 * @param ensuingDecisions a list of ensuing decisions
 * @param shortTexts the short texts (Kurztexte)
 * @param longTexts the long texts (Langtexte)
 * @param status the current status including publication and error status
 * @param note a note added to the documentation unit (Notiz)
 * @param contentRelatedIndexing the content related indexing data (Inhaltliche Erschließung)
 * @param caselawReferences a list of legal periodical caselaw references (Fundstellen)
 * @param literatureReferences a list of legal periodical literature references
 *     (Literaturfundstellen)
 * @param isEditable a flag indicating if the documentation unit is editable by the user
 */
@Builder(toBuilder = true)
public record DocumentationUnit(
    UUID uuid,
    Long version,
    @Size(min = 13, max = 14, message = "documentNumber has to be 13 or 14 characters long")
        String documentNumber,
    List<Attachment> attachments,
    @Valid CoreData coreData,
    List<PreviousDecision> previousDecisions,
    List<EnsuingDecision> ensuingDecisions,
    ShortTexts shortTexts,
    LongTexts longTexts,
    @Valid ManagementData managementData,
    Status status,
    String note,
    ContentRelatedIndexing contentRelatedIndexing,
    List<Reference> caselawReferences,
    List<Reference> literatureReferences,
    List<String> documentalists,
    InboxStatus inboxStatus,
    boolean isEditable,
    boolean isDeletable,
    Kind kind)
    implements Documentable {

  @Override
  public Kind kind() {
    return Kind.DOCUMENT_UNIT;
  }
}
