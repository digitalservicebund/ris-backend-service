package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitSearchResultDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchResult;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit.RelatedDocumentationUnitBuilder;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DocumentationUnitSearchResultTransformer {
  private DocumentationUnitSearchResultTransformer() {}

  public static DocumentationUnitSearchResult transformToDomain(
      DocumentationUnitSearchResultDTO documentationUnitSearchResultDTO) {
    if (documentationUnitSearchResultDTO == null) {
      return DocumentationUnitSearchResult.builder().build();
    }

    log.debug(
        "transfer database documentation unit '{}' to domain object",
        documentationUnitSearchResultDTO.getId());

    DocumentationUnitSearchResult.DocumentationUnitSearchResultBuilder builder =
        DocumentationUnitSearchResult.builder();

    builder
        .uuid(documentationUnitSearchResultDTO.getId())
        .documentNumber(documentationUnitSearchResultDTO.getDocumentNumber())
        .referencedDocumentationUnitId(documentationUnitSearchResultDTO.getId())
        .decisionDate(documentationUnitSearchResultDTO.getDecisionDate())
        .fileName(
            documentationUnitSearchResultDTO.getOriginalFileDocument() == null
                ? null
                : documentationUnitSearchResultDTO.getOriginalFileDocument().getFilename())
        .documentType(
            documentationUnitSearchResultDTO.getDocumentType() == null
                ? null
                : DocumentTypeTransformer.transformToDomain(
                    documentationUnitSearchResultDTO.getDocumentType()))
        .court(
            documentationUnitSearchResultDTO.getCourt() == null
                ? null
                : CourtTransformer.transformToDomain(documentationUnitSearchResultDTO.getCourt()))
        .fileNumber(
            documentationUnitSearchResultDTO.getFileNumbers() == null
                    || documentationUnitSearchResultDTO.getFileNumbers().isEmpty()
                ? null
                : documentationUnitSearchResultDTO.getFileNumbers().get(0).getValue())
        .status(
            documentationUnitSearchResultDTO.getStatus() == null
                    || documentationUnitSearchResultDTO.getStatus().isEmpty()
                ? null
                : Status.builder()
                    // TODO is the first status the most recent?
                    .publicationStatus(
                        documentationUnitSearchResultDTO.getStatus().get(0) == null // NOSONAR
                            // reason for NOSONAR: it's still readable and there is still a todo, so
                            // still object to change
                            ? null
                            : documentationUnitSearchResultDTO
                                .getStatus()
                                .get(0)
                                .getPublicationStatus())
                    .withError(
                        documentationUnitSearchResultDTO.getStatus().get(0) == null
                            || documentationUnitSearchResultDTO.getStatus().get(0).isWithError())
                    .build());

    return builder.build();
  }

  public static RelatedDocumentationUnit transformToRelatedDocumentation(
      DocumentationUnitSearchResultDTO documentationUnitSearchResultDTO) {

    RelatedDocumentationUnitBuilder<?, ?> builder =
        RelatedDocumentationUnit.builder()
            .uuid(documentationUnitSearchResultDTO.getId())
            .documentNumber(documentationUnitSearchResultDTO.getDocumentNumber())
            .court(CourtTransformer.transformToDomain(documentationUnitSearchResultDTO.getCourt()))
            .decisionDate(documentationUnitSearchResultDTO.getDecisionDate())
            .documentType(
                DocumentTypeTransformer.transformToDomain(
                    documentationUnitSearchResultDTO.getDocumentType()))
            .referenceFound(true);

    if (documentationUnitSearchResultDTO.getFileNumbers() != null
        && !documentationUnitSearchResultDTO.getFileNumbers().isEmpty()) {

      builder.fileNumber(documentationUnitSearchResultDTO.getFileNumbers().get(0).getValue());
    }

    return builder.build();
  }
}
