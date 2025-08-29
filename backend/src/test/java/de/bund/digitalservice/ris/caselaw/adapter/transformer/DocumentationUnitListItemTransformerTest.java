package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitListItemDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.SourceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.SourceValue;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DocumentationUnitListItemTransformerTest {
  @Test
  void testTransformToDomain_withDecision_shouldTransformAllFields() {
    UUID id = UUID.randomUUID();

    UUID previousDocumentationUnitProcessStepId = UUID.randomUUID();

    var previousDocumentationUnitProcessStepDTO =
        DocumentationUnitProcessStepDTO.builder()
            .processStep(
                ProcessStepDTO.builder()
                    .id(previousDocumentationUnitProcessStepId)
                    .name("Ersterfassung")
                    .abbreviation("EE")
                    .build())
            .build();

    var currentDocumentationUnitProcessStepDTO =
        DocumentationUnitProcessStepDTO.builder()
            .processStep(
                ProcessStepDTO.builder()
                    .id(UUID.randomUUID())
                    .name("QS Formal")
                    .abbreviation("QS")
                    .build())
            .build();

    DocumentationUnitListItemDTO currentDto =
        DecisionDTO.builder()
            .id(id)
            .note("a note")
            .court(CourtDTO.builder().type("LG").location("Berlin").build())
            .documentType(DocumentTypeDTO.builder().abbreviation("Urt").build())
            .fileNumbers(List.of(FileNumberDTO.builder().value("1 BvR 1234/19").build()))
            .date(LocalDate.parse("2021-01-01"))
            .scheduledPublicationDateTime(LocalDateTime.parse("2022-01-23T18:25:14"))
            .lastPublicationDateTime(LocalDateTime.parse("2022-01-22T18:27:18"))
            .judicialBody("1. Senat")
            .headnote("headnote")
            .creatingDocumentationOffice(
                DocumentationOfficeDTO.builder().abbreviation("DS").build())
            .processSteps(
                List.of(
                    currentDocumentationUnitProcessStepDTO,
                    currentDocumentationUnitProcessStepDTO,
                    previousDocumentationUnitProcessStepDTO))
            .source(
                List.of(
                    SourceDTO.builder().value(SourceValue.E).build(),
                    SourceDTO.builder().value(SourceValue.O).build()))
            .status(
                StatusDTO.builder()
                    .createdAt(Instant.now())
                    .publicationStatus(PublicationStatus.PUBLISHED)
                    .withError(false)
                    .build())
            .build();

    DocumentationUnitListItem documentationUnitListItem =
        DocumentationUnitListItemTransformer.transformToDomain(currentDto);

    // basic data
    assertThat(documentationUnitListItem.note()).isEqualTo("a note");
    assertThat(documentationUnitListItem.court())
        .isEqualTo(
            Court.builder()
                .type("LG")
                .location("Berlin")
                .label("LG Berlin")
                .jurisdictionType("")
                .region("")
                .build());
    assertThat(documentationUnitListItem.documentType())
        .isEqualTo(DocumentType.builder().jurisShortcut("Urt").build());
    assertThat(documentationUnitListItem.fileNumber()).isEqualTo("1 BvR 1234/19");
    assertThat(documentationUnitListItem.decisionDate()).isEqualTo(LocalDate.parse("2021-01-01"));
    assertThat(documentationUnitListItem.scheduledPublicationDateTime())
        .isEqualTo(LocalDateTime.parse("2022-01-23T18:25:14"));
    assertThat(documentationUnitListItem.lastPublicationDateTime())
        .isEqualTo(LocalDateTime.parse("2022-01-22T18:27:18"));
    assertThat(documentationUnitListItem.appraisalBody()).isEqualTo("1. Senat");
    assertThat(documentationUnitListItem.hasHeadnoteOrPrinciple()).isTrue();
    // source and creating doc office
    assertThat(documentationUnitListItem.creatingDocumentationOffice().abbreviation())
        .isEqualTo("DS");
    assertThat(documentationUnitListItem.source()).isEqualTo("E, O");
    // status
    assertThat(documentationUnitListItem.status().publicationStatus())
        .isEqualTo(PublicationStatus.PUBLISHED);
    assertThat(documentationUnitListItem.status().withError()).isFalse();
    // current doc process step
    assertThat(
            documentationUnitListItem.currentDocumentationUnitProcessStep().getProcessStep().uuid())
        .isEqualTo(currentDocumentationUnitProcessStepDTO.getProcessStep().getId());
    // previous process step
    assertThat(documentationUnitListItem.previousProcessStep().uuid())
        .isEqualTo(previousDocumentationUnitProcessStepId);
  }

  @Test
  void testTransformToDomain_withPendingProceeding_shouldTransformAllFields() {
    UUID id = UUID.randomUUID();
    DocumentationUnitListItemDTO currentDto =
        PendingProceedingDTO.builder()
            .id(id)
            .court(CourtDTO.builder().type("LG").location("Berlin").build())
            .documentType(DocumentTypeDTO.builder().abbreviation("Urt").build())
            .fileNumbers(List.of(FileNumberDTO.builder().value("1 BvR 1234/19").build()))
            .date(LocalDate.parse("2021-01-01"))
            .judicialBody("1. Senat")
            .resolutionNote("resolutionNode")
            .scheduledPublicationDateTime(LocalDateTime.parse("2022-01-23T18:25:14"))
            .lastPublicationDateTime(LocalDateTime.parse("2022-01-22T18:27:18"))
            .isResolved(true)
            .legalIssue("legalIssue")
            .admissionOfAppeal("admissionOfAppeal")
            .appellant("appellant")
            .resolutionDate(LocalDate.parse("2025-07-01"))
            .status(
                StatusDTO.builder()
                    .createdAt(Instant.now())
                    .publicationStatus(PublicationStatus.PUBLISHED)
                    .withError(false)
                    .build())
            .build();

    DocumentationUnitListItem documentationUnitListItem =
        DocumentationUnitListItemTransformer.transformToDomain(currentDto);

    // basic data
    assertThat(documentationUnitListItem.court())
        .isEqualTo(
            Court.builder()
                .type("LG")
                .location("Berlin")
                .label("LG Berlin")
                .jurisdictionType("")
                .region("")
                .build());
    assertThat(documentationUnitListItem.documentType())
        .isEqualTo(DocumentType.builder().jurisShortcut("Urt").build());
    assertThat(documentationUnitListItem.fileNumber()).isEqualTo("1 BvR 1234/19");
    assertThat(documentationUnitListItem.decisionDate()).isEqualTo(LocalDate.parse("2021-01-01"));
    assertThat(documentationUnitListItem.scheduledPublicationDateTime()).isNull();
    assertThat(documentationUnitListItem.lastPublicationDateTime()).isNull();
    assertThat(documentationUnitListItem.appraisalBody()).isEqualTo("1. Senat");
    assertThat(documentationUnitListItem.hasHeadnoteOrPrinciple()).isFalse();
    // status
    assertThat(documentationUnitListItem.status().publicationStatus())
        .isEqualTo(PublicationStatus.PUBLISHED);
    assertThat(documentationUnitListItem.status().withError()).isFalse();
    // resolution Date
    assertThat(documentationUnitListItem.resolutionDate()).isEqualTo("2025-07-01");
  }

  @Test
  void testTransformToDomain_withoutStatus_shouldTransformToNullStatus() {
    UUID id = UUID.randomUUID();

    DocumentationUnitListItemDTO currentDto = DecisionDTO.builder().id(id).status(null).build();

    DocumentationUnitListItem documentationUnitListItem =
        DocumentationUnitListItemTransformer.transformToDomain(currentDto);

    assertThat(documentationUnitListItem.status()).isNull();
  }

  @Test
  void testTransformToDomain_withoutNote_shouldHaveNoNote() {
    UUID id = UUID.randomUUID();
    DocumentationUnitListItemDTO currentDto = DecisionDTO.builder().id(id).note(null).build();

    DocumentationUnitListItem documentationUnitListItem =
        DocumentationUnitListItemTransformer.transformToDomain(currentDto);

    assertThat(documentationUnitListItem.note()).isNull();
  }

  @Test
  void testTransformToDomain_withOnlyImages_hasAttachmentsShouldBeFalse() {
    UUID id = UUID.randomUUID();
    DocumentationUnitListItemDTO currentDto =
        DecisionDTO.builder()
            .id(id)
            .attachments(
                List.of(
                    AttachmentDTO.builder().format("png").build(),
                    AttachmentDTO.builder().format("jpg").build()))
            .build();

    DocumentationUnitListItem documentationUnitListItem =
        DocumentationUnitListItemTransformer.transformToDomain(currentDto);

    assertThat(documentationUnitListItem.hasAttachments()).isFalse();
  }

  @Test
  void testTransformToDomain_withMixedAttachments_hasAttachmentsShouldBeTrue() {
    UUID id = UUID.randomUUID();
    DocumentationUnitListItemDTO currentDto =
        DecisionDTO.builder()
            .id(id)
            .attachments(
                List.of(
                    AttachmentDTO.builder().format("png").build(),
                    AttachmentDTO.builder().format("fmx").build()))
            .build();

    DocumentationUnitListItem documentationUnitListItem =
        DocumentationUnitListItemTransformer.transformToDomain(currentDto);

    assertThat(documentationUnitListItem.hasAttachments()).isTrue();
  }
}
