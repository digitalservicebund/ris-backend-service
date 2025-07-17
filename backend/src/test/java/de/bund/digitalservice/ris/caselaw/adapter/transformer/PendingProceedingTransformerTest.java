package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CaselawReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingDocumentNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LiteratureReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ManagementDataDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PreviousDecisionDTO;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.ManagementData;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceedingShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.ReferenceType;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PendingProceedingTransformerTest {

  @Test
  void testTransformToDomain_withPendingProceedingDTOIsNull_shouldThrowException() {
    assertThatThrownBy(() -> PendingProceedingTransformer.transformToDomain(null))
        .isInstanceOf(DocumentationUnitTransformerException.class)
        .hasMessageContaining("Pending proceeding is null and won't transform");
  }

  @Test
  void testTransformToDomain_withPendingProceedingFields() {
    PendingProceedingDTO pendingProceedingDTO =
        generateSimpleDTOBuilder()
            .appellant("appellant")
            .admissionOfAppeal("admission of appeal")
            .legalIssue("legal issue")
            .resolutionNote("resolution note")
            .isResolved(true)
            .resolutionDate(LocalDate.now())
            .build();
    PendingProceeding pendingProceeding =
        PendingProceedingTransformer.transformToDomain(pendingProceedingDTO);

    assertThat(pendingProceeding.coreData().isResolved()).isTrue();
    assertThat(pendingProceeding.coreData().resolutionDate()).isToday();

    assertThat(pendingProceeding.shortTexts().appellant()).isEqualTo("appellant");
    assertThat(pendingProceeding.shortTexts().admissionOfAppeal()).isEqualTo("admission of appeal");
    assertThat(pendingProceeding.shortTexts().legalIssue()).isEqualTo("legal issue");
    assertThat(pendingProceeding.shortTexts().resolutionNote()).isEqualTo("resolution note");
  }

  @Test
  void testTransformToDomain_withManagementData() {
    // Arrange
    Instant lastUpdatedAtDateTime = Instant.now();
    DocumentationOfficeDTO creatingAndUpdatingDocOffice =
        DocumentationOfficeDTO.builder().id(UUID.randomUUID()).abbreviation("BGH").build();
    String lastUpdatedByName = "Winnie Puuh";
    Instant createdAtDateTime = Instant.now();
    String createdByName = "I Aah";
    Instant firstPublishedAtDateTime = Instant.now();

    ManagementDataDTO managementDataDTO =
        ManagementDataDTO.builder()
            .lastUpdatedAtDateTime(lastUpdatedAtDateTime)
            .lastUpdatedByUserName(lastUpdatedByName)
            .lastUpdatedByDocumentationOffice(creatingAndUpdatingDocOffice)
            .createdAtDateTime(createdAtDateTime)
            .createdByUserName(createdByName)
            .createdByDocumentationOffice(creatingAndUpdatingDocOffice)
            .firstPublishedAtDateTime(firstPublishedAtDateTime)
            .build();
    PendingProceedingDTO pendingProceedingDTO =
        PendingProceedingDTO.builder().managementData(managementDataDTO).build();

    ManagementData expected =
        ManagementData.builder()
            .lastUpdatedAtDateTime(lastUpdatedAtDateTime)
            .lastUpdatedByName(lastUpdatedByName)
            .lastUpdatedByDocOffice(creatingAndUpdatingDocOffice.getAbbreviation())
            .createdAtDateTime(createdAtDateTime)
            .createdByName(createdByName)
            .createdByDocOffice(creatingAndUpdatingDocOffice.getAbbreviation())
            .firstPublishedAtDateTime(firstPublishedAtDateTime)
            .duplicateRelations(List.of())
            .borderNumbers(List.of())
            .build();
    User user =
        User.builder()
            .documentationOffice(
                DocumentationOffice.builder()
                    .id(creatingAndUpdatingDocOffice.getId())
                    .abbreviation(creatingAndUpdatingDocOffice.getAbbreviation())
                    .build())
            .build();

    // Act
    PendingProceeding pendingProceeding =
        PendingProceedingTransformer.transformToDomain(pendingProceedingDTO, user);

    // Assert
    assertThat(pendingProceeding.managementData()).isEqualTo(expected);
  }

  @Test
  void testTransformToDTO_withoutCoreData() {
    PendingProceedingDTO currentDto = PendingProceedingDTO.builder().build();
    PendingProceeding pendingProceeding = PendingProceeding.builder().build();

    PendingProceedingDTO resultDto =
        PendingProceedingTransformer.transformToDTO(currentDto, pendingProceeding);

    assertNull(resultDto.getJudicialBody());
    assertNull(resultDto.getDate());
    assertNull(resultDto.getDocumentType());
    assertNull(resultDto.getCourt());
    assertFalse(resultDto.isResolved());
    assertNull(resultDto.getResolutionDate());
  }

  @Test
  void testTransformToDTO_withCoreData() {
    PendingProceedingDTO currentDto = PendingProceedingDTO.builder().build();
    CoreData coreData =
        CoreData.builder()
            .appraisalBody("Test Body")
            .decisionDate(LocalDate.of(2023, 1, 15))
            .documentType(DocumentType.builder().uuid(UUID.randomUUID()).build())
            .court(Court.builder().id(UUID.randomUUID()).build())
            .isResolved(true) // Set to true here
            .resolutionDate(LocalDate.of(2023, 2, 1))
            .build();
    PendingProceeding pendingProceeding =
        PendingProceeding.builder()
            .uuid(UUID.randomUUID())
            .documentNumber("DN-123")
            .version(1L)
            .coreData(coreData)
            .build();

    PendingProceedingDTO resultDto =
        PendingProceedingTransformer.transformToDTO(currentDto, pendingProceeding);

    assertThat(resultDto.getId()).isEqualTo(pendingProceeding.uuid());
    assertThat(resultDto.getDocumentNumber()).isEqualTo(pendingProceeding.documentNumber());
    assertThat(resultDto.getVersion()).isEqualTo(pendingProceeding.version());
    assertThat(resultDto.getJudicialBody()).isEqualTo(coreData.appraisalBody());
    assertThat(resultDto.getDate()).isEqualTo(coreData.decisionDate());
    assertThat(resultDto.getDocumentType().getId()).isEqualTo(coreData.documentType().uuid());
    assertThat(resultDto.getCourt().getId()).isEqualTo(coreData.court().id());
  }

  @Test
  void testTransformToDomain_withCoreData_shouldTransformAllCoreDataRelevantForPendingProceeding() {
    // Arrange
    UUID decisionId = UUID.randomUUID();
    UUID documentTypeId = UUID.randomUUID();
    UUID courtId = UUID.randomUUID();
    UUID docOfficeId = UUID.randomUUID();
    String documentNumber = "DOCNUMBER1234";

    LocalDate decisionDate = LocalDate.of(2024, 5, 10);
    LocalDate deviatingDecisionDate = LocalDate.of(2024, 5, 10);
    String celexNumber = "CELEX-XYZ";
    String judicialBody = "Bundesverfassungsgericht";
    String fileNumber = "AZ-456";
    String deviatingFileNumber = "1B23/24";
    String courtLabel = "LG Berlin";

    PendingProceedingDTO pendingProceedingDTO =
        PendingProceedingDTO.builder()
            // --- fields from DocumentationUnitDTO parent (for builder) ---
            .id(decisionId)
            .documentNumber(documentNumber)
            .version(1L)
            .date(decisionDate)
            .documentType(DocumentTypeDTO.builder().id(documentTypeId).abbreviation("Urt").build())
            .court(CourtDTO.builder().id(courtId).type("BVerfG").build())
            .celexNumber(celexNumber)
            .judicialBody(judicialBody) // Mapped to coreData.appraisalBody
            .fileNumbers(
                List.of(
                    de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO.builder()
                        .rank(0L)
                        .value(fileNumber)
                        .build()))
            .deviatingFileNumbers(
                List.of(
                    DeviatingFileNumberDTO.builder().rank(0L).value(deviatingFileNumber).build()))
            .deviatingDates(
                List.of(DeviatingDateDTO.builder().rank(0L).value(deviatingDecisionDate).build()))
            .deviatingCourts(
                List.of(
                    de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingCourtDTO
                        .builder()
                        .rank(0L)
                        .value(courtLabel)
                        .build()))
            .documentationOffice(
                DocumentationOfficeDTO.builder().id(docOfficeId).abbreviation("DS").build())

            // --- Fields specifically from PendingProceedingDTO that contribute to CoreData ---
            .isResolved(true)
            .resolutionDate(LocalDate.of(2024, 5, 10))
            .build();

    // Act
    PendingProceeding pendingProceeding =
        PendingProceedingTransformer.transformToDomain(pendingProceedingDTO);

    // Assert general DocumentationUnit fields
    assertThat(pendingProceeding).isNotNull();
    assertThat(pendingProceeding.uuid()).isEqualTo(decisionId);
    assertThat(pendingProceeding.documentNumber()).isEqualTo(documentNumber);
    assertThat(pendingProceeding.version()).isEqualTo(1L);

    // Assert CoreData object itself
    CoreData coreData = pendingProceeding.coreData();
    assertThat(coreData).isNotNull();

    // --- Assert mutual CoreData fields that are transformed from DocumentableTransformer ---
    assertThat(coreData.celexNumber()).isEqualTo(celexNumber);
    assertThat(coreData.appraisalBody()).isEqualTo(judicialBody);
    assertThat(coreData.decisionDate()).isEqualTo(decisionDate);
    assertThat(coreData.documentType().jurisShortcut()).isEqualTo("Urt");
    assertThat(coreData.court().label()).isEqualTo("BVerfG");
    assertThat(coreData.fileNumbers().getFirst()).isEqualTo(fileNumber);
    assertThat(coreData.deviatingFileNumbers().getFirst()).isEqualTo(deviatingFileNumber);
    assertThat(coreData.deviatingCourts().getFirst()).isEqualTo(courtLabel);
    assertThat(coreData.deviatingDecisionDates().getFirst()).isEqualTo(deviatingDecisionDate);
    assertThat(coreData.documentationOffice()).isNotNull();
    assertThat(coreData.documentationOffice().id()).isEqualTo(docOfficeId);
    assertThat(coreData.documentationOffice().abbreviation()).isEqualTo("DS");

    // --- Assert CoreData fields that are transformed from PendingProceedingTransformer ---
    assertThat(coreData.isResolved()).isTrue();
    assertThat(coreData.resolutionDate()).isEqualTo("2024-05-10");
  }

  @Test
  void testTransformToDTO_withShortTexts() {
    PendingProceedingDTO currentDto = PendingProceedingDTO.builder().build();
    PendingProceedingShortTexts shortTexts =
        PendingProceedingShortTexts.builder().headline("Test Headline").build();
    PendingProceeding pendingProceeding =
        PendingProceeding.builder().shortTexts(shortTexts).build();

    PendingProceedingDTO resultDto =
        PendingProceedingTransformer.transformToDTO(currentDto, pendingProceeding);

    assertThat(resultDto.getHeadline()).isEqualTo(shortTexts.headline());
  }

  @Test
  void testTransformToDTO_withoutShortTexts() {
    PendingProceedingDTO currentDto = PendingProceedingDTO.builder().build();
    PendingProceeding pendingProceeding = PendingProceeding.builder().build();

    PendingProceedingDTO resultDto =
        PendingProceedingTransformer.transformToDTO(currentDto, pendingProceeding);

    assertNull(resultDto.getHeadline());
  }

  @Test
  void testTransformToDTO_withNullPendingProceeding_shouldThrowException() {
    PendingProceedingDTO currentDto = PendingProceedingDTO.builder().build();

    assertThatThrownBy(() -> PendingProceedingTransformer.transformToDTO(currentDto, null))
        .isInstanceOf(DocumentationUnitTransformerException.class)
        .hasMessageContaining("Pending proceeding is null and won't transform");
  }

  @Test
  void testTransformToDTO_withResolutionNoteLegalIssueAdmissionOfAppealAppellant() {
    PendingProceedingDTO currentDto = PendingProceedingDTO.builder().build();
    PendingProceedingShortTexts shortTexts =
        PendingProceedingShortTexts.builder()
            .headline("Test Headline")
            .resolutionNote("Resolution Note")
            .legalIssue("Legal Issue")
            .admissionOfAppeal("Admission of Appeal")
            .appellant("Appellant Name")
            .build();
    PendingProceeding pendingProceeding =
        PendingProceeding.builder().shortTexts(shortTexts).build();

    PendingProceedingDTO resultDto =
        PendingProceedingTransformer.transformToDTO(currentDto, pendingProceeding);

    assertThat(resultDto.getResolutionNote()).isEqualTo("Resolution Note");
    assertThat(resultDto.getLegalIssue()).isEqualTo("Legal Issue");
    assertThat(resultDto.getAdmissionOfAppeal()).isEqualTo("Admission of Appeal");
    assertThat(resultDto.getAppellant()).isEqualTo("Appellant Name");
  }

  @Test
  void testTransformToDTO_withCaselawReferences() {
    var uuid = UUID.randomUUID();
    PendingProceedingDTO currentDto =
        PendingProceedingDTO.builder()
            .caselawReferences(
                List.of(CaselawReferenceDTO.builder().id(uuid).documentationUnitRank(3).build()))
            .build();

    var updatedReferences =
        List.of(
            Reference.builder()
                .id(uuid)
                .referenceType(ReferenceType.CASELAW)
                .primaryReference(true)
                .build());
    PendingProceeding pendingProceeding =
        PendingProceeding.builder().caselawReferences(updatedReferences).build();

    PendingProceedingDTO resultDto =
        PendingProceedingTransformer.transformToDTO(currentDto, pendingProceeding);

    assertThat(resultDto.getCaselawReferences()).hasSize(1);
    assertThat(resultDto.getCaselawReferences().getFirst().getDocumentationUnitRank()).isOne();
    assertThat(resultDto.getCaselawReferences().getFirst().getId()).isEqualTo(uuid);
  }

  @Test
  void testTransformToDTO_withLiteratureReferences() {
    var uuid = UUID.randomUUID();
    PendingProceedingDTO currentDto =
        PendingProceedingDTO.builder()
            .literatureReferences(
                List.of(LiteratureReferenceDTO.builder().id(uuid).documentationUnitRank(3).build()))
            .build();

    var updatedReferences =
        List.of(Reference.builder().id(uuid).referenceType(ReferenceType.LITERATURE).build());
    PendingProceeding pendingProceeding =
        PendingProceeding.builder().literatureReferences(updatedReferences).build();

    PendingProceedingDTO resultDto =
        PendingProceedingTransformer.transformToDTO(currentDto, pendingProceeding);

    assertThat(resultDto.getLiteratureReferences()).hasSize(1);
    assertThat(resultDto.getLiteratureReferences().getFirst().getDocumentationUnitRank()).isOne();
    assertThat(resultDto.getLiteratureReferences().getFirst().getId()).isEqualTo(uuid);
  }

  @Test
  void testTransformToDTO_addPreviousDecisions() {
    PendingProceedingDTO currentDto = PendingProceedingDTO.builder().build();
    PendingProceeding pendingProceeding =
        PendingProceeding.builder()
            .previousDecisions(
                List.of(
                    de.bund.digitalservice.ris.caselaw.domain.PreviousDecision.builder()
                        .fileNumber("PrevFile1")
                        .build()))
            .build();

    PendingProceedingDTO resultDto =
        PendingProceedingTransformer.transformToDTO(currentDto, pendingProceeding);

    assertThat(resultDto.getPreviousDecisions()).hasSize(1);
    assertThat(resultDto.getPreviousDecisions().getFirst().getFileNumber()).isEqualTo("PrevFile1");
  }

  @Test
  void testTransformToDTO_removePreviousDecisions() {
    PendingProceedingDTO currentDto =
        PendingProceedingDTO.builder()
            .previousDecisions(List.of(PreviousDecisionDTO.builder().fileNumber("OldPrev").build()))
            .build();
    PendingProceeding pendingProceeding =
        PendingProceeding.builder().previousDecisions(List.of()).build();

    PendingProceedingDTO resultDto =
        PendingProceedingTransformer.transformToDTO(currentDto, pendingProceeding);

    assertThat(resultDto.getPreviousDecisions()).isEmpty();
  }

  @Test
  void testTransformToDomain_withDeviatingDocumentNumbers_shouldAdd() {
    PendingProceedingDTO decisionDTO =
        generateSimpleDTOBuilder()
            .deviatingDocumentNumbers(
                List.of(
                    DeviatingDocumentNumberDTO.builder().value("XXRE123456789").rank(1L).build(),
                    DeviatingDocumentNumberDTO.builder().value("XXRE234567890").rank(2L).build()))
            .build();

    PendingProceeding decision = PendingProceedingTransformer.transformToDomain(decisionDTO);

    assertThat(decision.coreData().deviatingDocumentNumbers())
        .containsExactly("XXRE123456789", "XXRE234567890");
  }

  @Test
  void testTransformToDomain_withoutDeviatingDocumentNumbers_shouldNotAdd() {
    PendingProceedingDTO decisionDTO = generateSimpleDTOBuilder().build();

    PendingProceeding decision = PendingProceedingTransformer.transformToDomain(decisionDTO);

    assertThat(decision.coreData().deviatingDocumentNumbers()).isEmpty();
  }

  @Test
  void testTransformToDTO_withDeviatingDocumentNumbers_shouldAdd() {
    PendingProceeding decision =
        PendingProceeding.builder()
            .coreData(
                CoreData.builder()
                    .deviatingDocumentNumbers(List.of("XXRE123456789", "XXRE234567890"))
                    .build())
            .build();

    DocumentationUnitDTO documentationUnitDTO =
        PendingProceedingTransformer.transformToDTO(generateSimpleDTOBuilder().build(), decision);

    assertThat(documentationUnitDTO.getDeviatingDocumentNumbers())
        .extracting("value")
        .containsExactly("XXRE123456789", "XXRE234567890");
  }

  @Test
  void testTransformToDTO_withoutDeviatingDocumentNumbers_shouldNotAdd() {
    PendingProceeding decision =
        PendingProceeding.builder().coreData(CoreData.builder().build()).build();

    DocumentationUnitDTO documentationUnitDTO =
        PendingProceedingTransformer.transformToDTO(generateSimpleDTOBuilder().build(), decision);

    assertThat(documentationUnitDTO.getDeviatingDocumentNumbers()).isEmpty();
  }

  private PendingProceedingDTO.PendingProceedingDTOBuilder<?, ?> generateSimpleDTOBuilder() {
    return PendingProceedingDTO.builder()
        .documentationOffice(DocumentationOfficeDTO.builder().abbreviation("doc office").build());
  }
}
