package de.bund.digitalservice.ris.caselaw;

import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.EXTERNAL_HANDOVER_PENDING;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JurisdictionTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RegionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** A static test class for generating default, commonly used entities for testing purposes. */
public class EntityBuilderTestUtil {

  private static final String DEFAULT_DOCUMENT_NUMBER = "1234567890126";

  public static RelatedDocumentationUnit createTestRelatedDocument() {
    return RelatedDocumentationUnit.builder()
        .uuid(UUID.fromString("e8c6f756-d6b2-4fa4-b751-e88c7c53bde4"))
        .documentNumber("YYTestDoc0013")
        .status(null)
        .fileNumber("AB 34/1")
        .court(createTestCourt())
        .build();
  }

  public static DocumentationUnitDTO createTestDocumentationUnitDTO() {
    return DecisionDTO.builder()
        .id(UUID.fromString("e8c6f756-d6b2-4fa4-b751-e88c7c53bde4"))
        .documentNumber("YYTestDoc0013")
        .court(createTestCourtDTO())
        .fileNumbers(List.of(createTestFileNumberDTO()))
        .build();
  }

  public static Court createTestCourt() {
    return Court.builder()
        .id(UUID.fromString("4e254f62-ce83-43fa-86c5-ecd9caa1d610"))
        .type("BGH")
        .label("BGH Berlin")
        .location("Berlin")
        .region("BE")
        .build();
  }

  public static CourtDTO createTestCourtDTO() {
    return CourtDTO.builder()
        .id(UUID.fromString("4e254f62-ce83-43fa-86c5-ecd9caa1d610"))
        .type("BGH")
        .location("Berlin")
        .jurisdictionType(JurisdictionTypeDTO.builder().build())
        .region(RegionDTO.builder().code("BE").build())
        .jurisId(0)
        .build();
  }

  public static FileNumberDTO createTestFileNumberDTO() {
    return FileNumberDTO.builder().value("AB 34/1").rank(0L).build();
  }

  public static LegalPeriodicalDTO createTestLegalPeriodicalDTO() {
    return LegalPeriodicalDTO.builder()
        .id(UUID.fromString("33333333-2222-3333-4444-555555555555"))
        .primaryReference(true)
        .title("Legal Periodical Title")
        .subtitle("Legal Periodical Subtitle")
        .abbreviation("LPA")
        .jurisId(0)
        .build();
  }

  public static LegalPeriodical createTestLegalPeriodical() {
    return LegalPeriodical.builder()
        .uuid(UUID.fromString("33333333-2222-3333-4444-555555555555"))
        .title("Legal Periodical Title")
        .subtitle("Legal Periodical Subtitle")
        .abbreviation("LPA")
        .primaryReference(true)
        .build();
  }

  public static DocumentTypeDTO createTestDocumentTypeDTO() {
    return DocumentTypeDTO.builder()
        .id(UUID.fromString("33333333-1111-3333-4444-555555555555"))
        .abbreviation("Auf")
        .label("Aufsatz")
        .build();
  }

  public static DocumentType createTestDocumentType() {
    return DocumentType.builder()
        .uuid(UUID.fromString("33333333-1111-3333-4444-555555555555"))
        .label("Aufsatz")
        .jurisShortcut("Auf")
        .build();
  }

  public static DocumentationUnitDTO createAndSaveDecision(
      DatabaseDocumentationUnitRepository repository, DocumentationOfficeDTO documentationOffice) {
    return createAndSaveDecision(
        repository,
        DecisionDTO.builder()
            .documentNumber(DEFAULT_DOCUMENT_NUMBER)
            .documentationOffice(documentationOffice),
        null);
  }

  public static DocumentationUnitDTO createAndSaveDecision(
      DatabaseDocumentationUnitRepository repository,
      DocumentationOfficeDTO documentationOffice,
      DocumentationOfficeDTO creatingDocumentationOffice,
      String documentNumber) {

    return createAndSaveDecision(
        repository,
        DecisionDTO.builder()
            .documentNumber(documentNumber)
            .documentationOffice(documentationOffice)
            .creatingDocumentationOffice(creatingDocumentationOffice),
        EXTERNAL_HANDOVER_PENDING);
  }

  public static DecisionDTO createAndSaveDecision(
      DatabaseDocumentationUnitRepository repository,
      DocumentationOfficeDTO documentationOffice,
      String documentNumber) {
    return createAndSaveDecision(
        repository,
        DecisionDTO.builder()
            .documentNumber(documentNumber)
            .documentationOffice(documentationOffice),
        null);
  }

  public static DocumentationUnitDTO createAndSaveDecision(
      DatabaseDocumentationUnitRepository repository,
      DecisionDTO.DecisionDTOBuilder<?, ?> builder) {
    return createAndSaveDecision(repository, builder, null);
  }

  public static DecisionDTO createAndSaveDecision(
      DatabaseDocumentationUnitRepository repository,
      DecisionDTO.DecisionDTOBuilder<?, ?> builder,
      PublicationStatus publicationStatus) {
    return createAndSaveDecision(repository, builder, publicationStatus, false);
  }

  public static DecisionDTO createAndSaveDecision(
      DatabaseDocumentationUnitRepository repository,
      DecisionDTO.DecisionDTOBuilder<?, ?> builder,
      PublicationStatus publicationStatus,
      boolean errorStatus) {

    var dtoBeforeSave = builder.build();

    // FileNumbers and attachments need back reference to docUnit -> needs to be saved first without
    // them
    var fileNumbers = dtoBeforeSave.getFileNumbers();
    dtoBeforeSave.setFileNumbers(null);
    var deviatingFileNumbers = dtoBeforeSave.getDeviatingFileNumbers();
    dtoBeforeSave.setDeviatingFileNumbers(null);
    var attachments = dtoBeforeSave.getAttachments();
    dtoBeforeSave.setAttachments(null);

    DecisionDTO dto = repository.save(dtoBeforeSave);

    fileNumbers.forEach(fn -> fn.setDocumentationUnit(dto));
    deviatingFileNumbers.forEach(fn -> fn.setDocumentationUnit(dto));
    attachments.forEach(a -> a.setDocumentationUnit(dto));
    dto.setFileNumbers(fileNumbers);
    dto.setDeviatingFileNumbers(deviatingFileNumbers);
    dto.setAttachments(attachments);

    return repository.save(
        dto.toBuilder()
            .status(
                StatusDTO.builder()
                    .publicationStatus(
                        publicationStatus != null ? publicationStatus : PublicationStatus.PUBLISHED)
                    .createdAt(Instant.now())
                    .withError(errorStatus)
                    .documentationUnit(dto)
                    .build())
            .build());
  }

  public static PendingProceedingDTO createAndSavePendingProceeding(
      DatabaseDocumentationUnitRepository repository, DocumentationOfficeDTO documentationOffice) {
    return createAndSavePendingProceeding(
        repository,
        PendingProceedingDTO.builder()
            .documentNumber(DEFAULT_DOCUMENT_NUMBER)
            .documentationOffice(documentationOffice),
        null);
  }

  public static PendingProceedingDTO createAndSavePendingProceeding(
      DatabaseDocumentationUnitRepository repository,
      DocumentationOfficeDTO documentationOffice,
      String documentNumber) {
    return createAndSavePendingProceeding(
        repository,
        PendingProceedingDTO.builder()
            .documentNumber(documentNumber)
            .documentationOffice(documentationOffice),
        null);
  }

  public static PendingProceedingDTO createAndSavePendingProceeding(
      DatabaseDocumentationUnitRepository repository,
      PendingProceedingDTO.PendingProceedingDTOBuilder<?, ?> builder) {
    return createAndSavePendingProceeding(repository, builder, null, false);
  }

  public static PendingProceedingDTO createAndSavePendingProceeding(
      DatabaseDocumentationUnitRepository repository,
      PendingProceedingDTO.PendingProceedingDTOBuilder<?, ?> builder,
      PublicationStatus publicationStatus) {
    return createAndSavePendingProceeding(repository, builder, publicationStatus, false);
  }

  public static PendingProceedingDTO createAndSavePendingProceeding(
      DatabaseDocumentationUnitRepository repository,
      PendingProceedingDTO.PendingProceedingDTOBuilder<?, ?> builder,
      PublicationStatus publicationStatus,
      boolean errorStatus) {

    var dtoBeforeSave = builder.build();

    // FileNumbers need back reference to docUnit -> needs to be saved first without them
    var fileNumbers = dtoBeforeSave.getFileNumbers();
    dtoBeforeSave.setFileNumbers(null);
    var deviatingFileNumbers = dtoBeforeSave.getDeviatingFileNumbers();
    dtoBeforeSave.setDeviatingFileNumbers(null);

    PendingProceedingDTO dto = repository.save(dtoBeforeSave);

    fileNumbers.forEach(fn -> fn.setDocumentationUnit(dto));
    deviatingFileNumbers.forEach(fn -> fn.setDocumentationUnit(dto));
    dto.setFileNumbers(fileNumbers);
    dto.setDeviatingFileNumbers(deviatingFileNumbers);

    return repository.save(
        dto.toBuilder()
            .status(
                StatusDTO.builder()
                    .publicationStatus(
                        publicationStatus != null ? publicationStatus : PublicationStatus.PUBLISHED)
                    .createdAt(Instant.now())
                    .withError(errorStatus)
                    .documentationUnit(dto)
                    .build())
            .build());
  }
}
