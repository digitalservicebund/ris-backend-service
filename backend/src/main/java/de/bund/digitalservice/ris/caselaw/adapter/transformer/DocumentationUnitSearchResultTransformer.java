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

  /**
   * Transforms a documentation unit search result object from its database representation into a
   * documentation unit domain object that is suitable to be consumed by clients of the REST
   * service.
   *
   * @param documentationUnitSearchResultDTO the database documentation unit search result
   * @return DocumentationUnitSearchResult the domain documentation unit search result
   */
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
        .appraisalBody(documentationUnitSearchResultDTO.getJudicialBody())
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

  /**
   * Transforms a documentation unit search result object from its database representation into a
   * domain object of related (linked) documentation unit, that is suitable to be consumed by
   * clients of the REST service.
   *
   * @param documentationUnitSearchResultDTO A database representation of a documentation unit
   *     search result, containing only relevant information
   * @return RelatedDocumentationUnit A representation of a linked documentation unit, containing
   *     only relevant data
   */
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
