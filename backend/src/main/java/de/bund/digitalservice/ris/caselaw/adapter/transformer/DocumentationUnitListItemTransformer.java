package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitListItemDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ManagementDataDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcedureDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit.RelatedDocumentationUnitBuilder;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DocumentationUnitListItemTransformer {
  private DocumentationUnitListItemTransformer() {}

  /**
   * Transforms a documentation unit list item object from its database representation into a
   * documentation unit domain object that is suitable to be consumed by clients of the REST
   * service.
   *
   * @param documentationUnitListItemDTO the database documentation unit search result
   * @return DocumentationUnitListItem the domain documentation unit search result
   */
  public static DocumentationUnitListItem transformToDomain(
      DocumentationUnitListItemDTO documentationUnitListItemDTO) {
    if (documentationUnitListItemDTO == null) {
      return DocumentationUnitListItem.builder().build();
    }

    log.debug(
        "transfer database documentation unit '{}' to domain object",
        documentationUnitListItemDTO.getId());

    DocumentationUnitListItem.DocumentationUnitListItemBuilder builder =
        DocumentationUnitListItem.builder();

    builder
        .uuid(documentationUnitListItemDTO.getId())
        .documentNumber(documentationUnitListItemDTO.getDocumentNumber())
        .decisionDate(documentationUnitListItemDTO.getDate())
        .resolutionDate(documentationUnitListItemDTO.getResolutionDate())
        .scheduledPublicationDateTime(
            documentationUnitListItemDTO.getScheduledPublicationDateTime())
        .lastPublicationDateTime(documentationUnitListItemDTO.getLastPublicationDateTime())
        .appraisalBody(documentationUnitListItemDTO.getJudicialBody())
        .hasHeadnoteOrPrinciple(hasHeadnoteOrPrinciple(documentationUnitListItemDTO))
        .hasAttachments(
            !documentationUnitListItemDTO.getAttachments().stream()
                .filter(
                    attachmentDTO ->
                        "fmx".equals(attachmentDTO.getFormat())
                            || "docx".equals(attachmentDTO.getFormat()))
                .toList()
                .isEmpty())
        .documentType(
            DocumentTypeTransformer.transformToDomain(
                documentationUnitListItemDTO.getDocumentType()))
        .court(CourtTransformer.transformToDomain(documentationUnitListItemDTO.getCourt()))
        .fileNumber(
            documentationUnitListItemDTO.getFileNumbers() == null
                    || documentationUnitListItemDTO.getFileNumbers().isEmpty()
                ? null
                : documentationUnitListItemDTO.getFileNumbers().get(0).getValue())
        .status(StatusTransformer.transformToDomain(documentationUnitListItemDTO.getStatus()))
        .note(documentationUnitListItemDTO.getNote())
        .currentDocumentationUnitProcessStep(
            getCurrentDocumentationUnitProcessStep(documentationUnitListItemDTO.getProcessSteps()))
        .creatingDocumentationOffice(
            documentationUnitListItemDTO.getCreatingDocumentationOffice() == null
                ? null
                : DocumentationOfficeTransformer.transformToDomain(
                    documentationUnitListItemDTO.getCreatingDocumentationOffice()))
        .documentationOffice(
            DocumentationOfficeTransformer.transformToDomain(
                documentationUnitListItemDTO.getDocumentationOffice()))
        .assignedUserGroup(
            Optional.ofNullable(documentationUnitListItemDTO.getProcedure())
                .map(ProcedureDTO::getUserGroupDTO)
                .map(UserGroupTransformer::transformToDomain)
                .orElse(null))
        .source(
            documentationUnitListItemDTO.getSource().stream()
                .map(
                    source ->
                        Optional.ofNullable(source.getReference())
                            .map(
                                referenceDTO ->
                                    referenceDTO.getLegalPeriodicalRawValue()
                                        + " "
                                        + referenceDTO.getCitation())
                            .orElse(String.valueOf(source.getValue())))
                .collect(Collectors.joining(", ")))
        .previousProcessStep(
            getPreviousProcessStep(documentationUnitListItemDTO.getProcessSteps()));

    ManagementDataDTO managementData = documentationUnitListItemDTO.getManagementData();
    if (managementData != null) {
      builder.createdAt(managementData.getCreatedAtDateTime());
    }

    return builder.build();
  }

  /**
   * Checks if a headnote or a guiding principle is given in a documentation unit, to display the
   * information in a list
   *
   * @param documentationUnitListItemDTO documentation unit to check for headline or principle
   * @return a boolean value if either the headnote or the guiding principle are filled in (i.e. one
   *     or both of these fields)
   */
  private static boolean hasHeadnoteOrPrinciple(
      DocumentationUnitListItemDTO documentationUnitListItemDTO) {

    String headnote = documentationUnitListItemDTO.getHeadnote();
    String guidingPrinciple = documentationUnitListItemDTO.getGuidingPrinciple();

    return !(headnote == null || headnote.isEmpty())
        || !(guidingPrinciple == null || guidingPrinciple.isEmpty());
  }

  /**
   * Transforms a documentation unit list item object from its database representation into a domain
   * object of related (linked) documentation unit, that is suitable to be consumed by clients of
   * the REST service.
   *
   * @param documentationUnitListItemDTO A database representation of a documentation unit search
   *     result, containing only relevant information
   * @return RelatedDocumentationUnit A representation of a linked documentation unit, containing
   *     only relevant data
   */
  public static RelatedDocumentationUnit transformToRelatedDocumentation(
      DocumentationUnitListItemDTO documentationUnitListItemDTO) {

    RelatedDocumentationUnitBuilder<?, ?> builder =
        RelatedDocumentationUnit.builder()
            .documentNumber(documentationUnitListItemDTO.getDocumentNumber())
            .court(CourtTransformer.transformToDomain(documentationUnitListItemDTO.getCourt()))
            .status(getStatus(documentationUnitListItemDTO))
            .decisionDate(documentationUnitListItemDTO.getDate())
            .documentType(
                DocumentTypeTransformer.transformToDomain(
                    documentationUnitListItemDTO.getDocumentType()))
            .documentationOffice(
                DocumentationOfficeTransformer.transformToDomain(
                    documentationUnitListItemDTO.getDocumentationOffice()))
            .creatingDocOffice(
                DocumentationOfficeTransformer.transformToDomain(
                    documentationUnitListItemDTO.getCreatingDocumentationOffice()));

    if (documentationUnitListItemDTO.getFileNumbers() != null
        && !documentationUnitListItemDTO.getFileNumbers().isEmpty()) {

      builder.fileNumber(documentationUnitListItemDTO.getFileNumbers().get(0).getValue());
    }

    return builder.build();
  }

  /**
   * Iterate backwards to find the previous process if the process id is different then the last one
   *
   * @param documentationUnitProcessStepsDTOs of the list item
   * @return the previous unique process step
   */
  private static ProcessStep getPreviousProcessStep(
      List<DocumentationUnitProcessStepDTO> documentationUnitProcessStepsDTOs) {

    if (documentationUnitProcessStepsDTOs == null) return null;

    if (documentationUnitProcessStepsDTOs.size() < 2) return null;

    UUID lastId =
        Optional.ofNullable(documentationUnitProcessStepsDTOs.getLast())
            .map(DocumentationUnitProcessStepDTO::getProcessStep)
            .map(ProcessStepDTO::getId)
            .orElse(null);

    return documentationUnitProcessStepsDTOs
        .subList(0, documentationUnitProcessStepsDTOs.size() - 1) // exclude last one
        .reversed()
        .stream()
        .map(DocumentationUnitProcessStepDTO::getProcessStep)
        .filter(Objects::nonNull)
        .filter(processStepDto -> !Objects.equals(processStepDto.getId(), lastId))
        .map(ProcessStepTransformer::toDomain)
        .findFirst()
        .orElse(null);
  }

  private static DocumentationUnitProcessStep getCurrentDocumentationUnitProcessStep(
      List<DocumentationUnitProcessStepDTO> documentationUnitProcessStepsDTOs) {

    return Optional.ofNullable(documentationUnitProcessStepsDTOs)
        .filter(steps -> !steps.isEmpty())
        .map(List::getLast)
        .map(DocumentationUnitProcessStepTransformer::toDomain)
        .orElse(null);
  }

  private static Status getStatus(DocumentationUnitListItemDTO documentationUnitListItemDTO) {
    return StatusTransformer.transformToDomain(documentationUnitListItemDTO.getStatus());
  }
}
