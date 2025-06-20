package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDuplicateCheckService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseFileNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.CourtTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DecisionTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelation;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatus;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.Builder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = {"classpath:courts_init.sql", "classpath:document_types.sql"})
@Sql(
    scripts = {"classpath:courts_cleanup.sql", "classpath:document_types_cleanup.sql"},
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DuplicateCheckIntegrationTest extends BaseIntegrationTest {
  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseFileNumberRepository fileNumberRepository;
  @Autowired private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseCourtRepository databaseCourtRepository;
  @Autowired private DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;
  @Autowired private DuplicateRelationRepository duplicateRelationRepository;
  @Autowired private DocumentationUnitService documentationUnitService;
  @Autowired private DatabaseDuplicateCheckService duplicateCheckService;

  private DocumentationOffice docOffice;
  private DocumentationOfficeDTO documentationOffice;
  private static final DocumentType documentType1 =
      DocumentType.builder().uuid(UUID.fromString("0f382996-497f-4c2f-9a30-1c73d8ac0a87")).build();
  private static final DocumentType documentType2 =
      DocumentType.builder().uuid(UUID.fromString("11defe05-cd4d-43e5-a07e-06c611b81a26")).build();
  private static final Court courtAgAachen =
      Court.builder().id(UUID.fromString("46301f85-9bd2-4690-a67f-f9fdfe725de3")).build();
  private static final Court courtOlgDresden =
      Court.builder().id(UUID.fromString("12e9f671-6a5c-4ec7-9b57-3fafdefd7a49")).build();

  @BeforeEach
  void setUp() {
    documentationOffice =
        documentationOfficeRepository.findByAbbreviation(buildDSDocOffice().abbreviation());

    docOffice = DocumentationOfficeTransformer.transformToDomain(documentationOffice);
  }

  @AfterEach
  void cleanUp() {
    fileNumberRepository.deleteAll();
    repository.deleteAll();
    databaseDocumentTypeRepository.deleteAll();
    duplicateRelationRepository.deleteAll();
  }

  @Nested
  class DuplicateCheckServiceTest {
    @Test
    void checkDuplicates_withoutFileNumbersOrEclis_shouldDoNothing()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitWithoutFileNumbers =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .documentType(documentType1)
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkDuplicates(docUnitWithoutFileNumbers.getDocumentNumber());

      var foundDocUnit =
          (Decision) documentationUnitService.getByUuid(docUnitWithoutFileNumbers.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).isEmpty();
      assertThat(foundDocUnit.managementData().duplicateRelations()).isEmpty();
    }

    @Test
    void checkDuplicates_withEmptyDocUnit_shouldDoNothing()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitWithoutFileNumbers =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(CreationParameters.builder().documentNumber("DocumentNumb1").build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkDuplicates(docUnitWithoutFileNumbers.getDocumentNumber());

      var foundDocUnit =
          (Decision) documentationUnitService.getByUuid(docUnitWithoutFileNumbers.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).isEmpty();
      assertThat(foundDocUnit.managementData().duplicateRelations()).isEmpty();
    }

    @Test
    void checkDuplicates_withoutFoundDuplicates_shouldDoNothing()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));

      // create non duplicate docUnit
      generateNewDocumentationUnit(
          docOffice,
          Optional.of(
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .fileNumbers(List.of("AZ-Different"))
                  .documentType(documentType1)
                  .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());

      var initialDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).isEmpty();
      assertThat(((Decision) initialDocUnit).managementData().duplicateRelations()).isEmpty();
    }

    @Test
    void checkDuplicates_withoutExistingDuplicates_shouldCreateNewDuplicateWithPendingStatus()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));

      var docUnitDuplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());

      var foundDocUnit = (Decision) documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      DuplicateRelation duplicate = foundDocUnit.managementData().duplicateRelations().getFirst();
      assertThat(duplicate.documentNumber()).isEqualTo(docUnitDuplicateDTO.getDocumentNumber());
      assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.PENDING);
    }

    @Test
    void checkDuplicates_withMatchingDeviatingFileNumber_shouldCreateNewDuplicateWithPendingStatus()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .deviatingFileNumbers(List.of("AZ-123-match", "AZ-888", "AZ-999"))
                      .documentType(documentType1)
                      .build()));

      var docUnitDuplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123-match", "AZ-777", "AZ-555"))
                      .documentType(documentType1)
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());

      var foundDocUnit = (Decision) documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      DuplicateRelation duplicate = foundDocUnit.managementData().duplicateRelations().getFirst();
      assertThat(duplicate.documentNumber()).isEqualTo(docUnitDuplicateDTO.getDocumentNumber());
      assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.PENDING);
    }

    @Test
    void
        checkDuplicates_withoutExistingDuplicatesAndIsJdvDuplicateCheckActiveTrue_shouldCreateNewDuplicateWithPendingStatus()
            throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));

      var docUnitDuplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .isJdvDuplicateCheckActive(true)
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());

      var foundDocUnit = (Decision) documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      DuplicateRelation duplicate = foundDocUnit.managementData().duplicateRelations().getFirst();
      assertThat(duplicate.documentNumber()).isEqualTo(docUnitDuplicateDTO.getDocumentNumber());
      assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.PENDING);
    }

    @Test
    void
        checkDuplicates_withoutExistingDuplicatesAndIsJdvDuplicateCheckActiveTrueAndLockedStatus_shouldCreateNewDuplicateWithIgnoredStatus()
            throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .isJdvDuplicateCheckActive(true)
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));

      var docUnitDuplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .publicationStatus(PublicationStatus.LOCKED)
                      .documentType(documentType1)
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());

      var foundDocUnit = (Decision) documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      DuplicateRelation duplicate = foundDocUnit.managementData().duplicateRelations().getFirst();
      assertThat(duplicate.documentNumber()).isEqualTo(docUnitDuplicateDTO.getDocumentNumber());
      assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.IGNORED);
    }

    @Test
    void
        checkDuplicates_withoutExistingDuplicatesAndIsJdvDuplicateCheckActiveTrueAndDuplicatedStatus_shouldCreateNewDuplicateWithIgnoredStatus()
            throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .publicationStatus(PublicationStatus.DUPLICATED)
                      .documentType(documentType1)
                      .build()));

      var docUnitDuplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .isJdvDuplicateCheckActive(true)
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());

      var foundDocUnit = (Decision) documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      DuplicateRelation duplicate = foundDocUnit.managementData().duplicateRelations().getFirst();
      assertThat(duplicate.documentNumber()).isEqualTo(docUnitDuplicateDTO.getDocumentNumber());
      assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.IGNORED);
    }

    @Test
    void
        checkDuplicates_withoutExistingDuplicatesAndIsJdvDuplicateCheckActiveFalse_shouldCreateNewDuplicateWithIgnoredStatus()
            throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));

      var docUnitDuplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .isJdvDuplicateCheckActive(false)
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());

      var foundDocUnit = (Decision) documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      DuplicateRelation duplicate = foundDocUnit.managementData().duplicateRelations().getFirst();
      assertThat(duplicate.documentNumber()).isEqualTo(docUnitDuplicateDTO.getDocumentNumber());
      assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.IGNORED);
    }

    @Test
    void
        checkDuplicates_withExistingDuplicatesAndIsJdvDuplicateCheckActiveFalse_shouldUpdateStatusToIgnored()
            throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));

      var duplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Create duplicate with pending status
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());
      assertThat(
              ((Decision) documentationUnitService.getByUuid(docUnitToBeChecked.getId()))
                  .managementData()
                  .duplicateRelations()
                  .getFirst()
                  .status())
          .isEqualTo(DuplicateRelationStatus.PENDING);

      // change isJdvDuplicateCheckActive from null to false
      duplicateDTO.setIsJdvDuplicateCheckActive(false);
      databaseDocumentationUnitRepository.save(duplicateDTO);

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());
      var foundDocUnit = (Decision) documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      DuplicateRelation duplicate = foundDocUnit.managementData().duplicateRelations().getFirst();
      assertThat(duplicate.documentNumber()).isEqualTo(duplicateDTO.getDocumentNumber());
      assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.IGNORED);
    }

    @Test
    void checkDuplicates_withExistingDuplicates_shouldDeleteOutdatedDuplicate()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));

      generateNewDocumentationUnit(
          docOffice,
          Optional.of(
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .fileNumbers(List.of("AZ-123"))
                  .documentType(documentType1)
                  .build()));
      var duplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb3")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));
      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Create duplicates
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());
      assertThat(
              ((Decision) documentationUnitService.getByUuid(docUnitToBeChecked.getId()))
                  .managementData()
                  .duplicateRelations()
                  .getFirst()
                  .status())
          .isEqualTo(DuplicateRelationStatus.PENDING);
      assertThat(duplicateRelationRepository.findAll()).hasSize(2);

      // change decisionDate in second duplicate
      duplicateDTO.setDate(LocalDate.of(2022, 2, 22));
      databaseDocumentationUnitRepository.save(duplicateDTO);

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());
      var foundDocUnit = (Decision) documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      assertThat(foundDocUnit.managementData().duplicateRelations().getFirst().documentNumber())
          .isEqualTo("DocumentNumb2");
    }

    @Test
    void deleteDocUnit_withDuplicateRelation_shouldDeleteDuplicateRelation()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));

      var duplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));
      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Create duplicates
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());
      assertThat(
              ((Decision) documentationUnitService.getByUuid(docUnitToBeChecked.getId()))
                  .managementData()
                  .duplicateRelations())
          .hasSize(1);
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);

      // Act
      documentationUnitService.deleteByUuid(duplicateDTO.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).isEmpty();
      assertThat(
              ((Decision) documentationUnitService.getByUuid(docUnitToBeChecked.getId()))
                  .managementData()
                  .duplicateRelations())
          .isEmpty();
    }

    @Test
    void
        checkDuplicates_withUnpublishedDocUnitFromOtherDocOffice_shouldBeFilterOutDuplicateWarnings()
            throws DocumentationUnitNotExistsException {

      var bghDocumentationOffice = documentationOfficeRepository.findByAbbreviation("BGH");

      var bghDocOffice = DocumentationOfficeTransformer.transformToDomain(bghDocumentationOffice);

      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));

      generateNewDocumentationUnit(
          bghDocOffice,
          Optional.of(
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .fileNumbers(List.of("AZ-123"))
                  .publicationStatus(PublicationStatus.UNPUBLISHED)
                  .documentType(documentType1)
                  .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Create duplicates
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());
      // Although a duplicate relation is created ...
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      // ... it won't be sent to the frontend
      assertThat(
              ((Decision) documentationUnitService.getByUuid(docUnitToBeChecked.getId()))
                  .managementData()
                  .duplicateRelations())
          .isEmpty();
    }

    @Test
    void checkDuplicates_whenRequestingDocUnit_shouldIncludeNewWarningsInResponse() {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));

      var docUnitDuplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act: GET-requesting a doc unit should trigger check
      var docUnitResponse =
          risWebTestClient
              .withDefaultLogin()
              .get()
              .uri("/api/v1/caselaw/documentunits/" + docUnitToBeChecked.getDocumentNumber())
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(Decision.class)
              .returnResult()
              .getResponseBody();

      // Assert: GET response should include duplicate relations
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      List<DuplicateRelation> duplicates = docUnitResponse.managementData().duplicateRelations();
      assertThat(duplicates).hasSize(1);
      assertThat(duplicates.getFirst().documentNumber())
          .isEqualTo(docUnitDuplicateDTO.getDocumentNumber());
      assertThat(duplicates.getFirst().status()).isEqualTo(DuplicateRelationStatus.PENDING);
    }

    @Test
    void setStatus_withExistingPendingDuplicateRelation_shouldUpdateStatusToIgnored()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var original =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));

      var duplicate =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .build()));

      duplicateCheckService.checkDuplicates(original.getDocumentNumber());
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      var pendingDuplicateRelation = duplicateRelationRepository.findAll().getFirst();
      assertThat(pendingDuplicateRelation.getRelationStatus())
          .isEqualTo(DuplicateRelationStatus.PENDING);

      // Act
      duplicateCheckService.updateDuplicateStatus(
          original.getDocumentNumber(),
          duplicate.getDocumentNumber(),
          DuplicateRelationStatus.IGNORED);

      // Assert
      assertThat(
              ((Decision) documentationUnitService.getByUuid(original.getId()))
                  .managementData()
                  .duplicateRelations()
                  .getFirst()
                  .status())
          .isEqualTo(DuplicateRelationStatus.IGNORED);
    }

    @Test
    void setStatus_withoutExistingDuplicateRelation_shouldThrow() {
      // Arrange
      generateNewDocumentationUnit(
          docOffice,
          Optional.of(
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .fileNumbers(List.of("AZ-123"))
                  .build()));

      generateNewDocumentationUnit(
          docOffice,
          Optional.of(
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("DIFFERENT"))
                  .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act + Assert
      assertThatThrownBy(
              () ->
                  duplicateCheckService.updateDuplicateStatus(
                      "DocumentNumb1", "DocumentNumb2", DuplicateRelationStatus.IGNORED))
          .isInstanceOf(EntityNotFoundException.class);
    }
  }

  @Nested
  class FindDuplicatesQueryTest {
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideTestCasesWithDuplicates")
    void findDuplicateTests(
        String testName, CreationParameters firstParams, CreationParameters secondParams)
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked = generateNewDocumentationUnit(docOffice, Optional.of(firstParams));
      generateNewDocumentationUnit(docOffice, Optional.of(secondParams));

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());
      var foundDocUnit = (Decision) documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      assertThat(foundDocUnit.managementData().duplicateRelations().getFirst().documentNumber())
          .isEqualTo(secondParams.documentNumber);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideTestCasesWithoutDuplicates")
    void findNoDuplicateTests(
        String testName, CreationParameters firstParams, CreationParameters secondParams)
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked = generateNewDocumentationUnit(docOffice, Optional.of(firstParams));
      generateNewDocumentationUnit(docOffice, Optional.of(secondParams));

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());
      var foundDocUnit = (Decision) documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).isEmpty();
      assertThat(foundDocUnit.managementData().duplicateRelations()).isEmpty();
    }

    @Test
    void checkDuplicates_pendingProceedingWithFileNumberAndDate_shouldDoNothing()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var dto1 =
          repository.save(
              PendingProceedingDTO.builder()
                  .documentationOffice(documentationOffice)
                  .date(LocalDate.of(2023, 12, 11))
                  .documentNumber("DocumentNumb1")
                  .build());
      var pendingProceedingDTO =
          dto1.toBuilder()
              .fileNumbers(
                  List.of(
                      FileNumberDTO.builder()
                          .rank(0L)
                          .value("123")
                          .documentationUnit(dto1)
                          .build()))
              .build();
      var decision =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .deviatingDecisionDates(List.of(LocalDate.of(2023, 12, 11)))
                      .documentNumber("DocumentNumb2")
                      .fileNumbers(List.of("123"))
                      .build()));

      repository.save(pendingProceedingDTO);

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkDuplicates("DocumentNumb2");

      var foundDocUnit = (Decision) documentationUnitService.getByUuid(decision.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).isEmpty();
      assertThat(foundDocUnit.managementData().duplicateRelations()).isEmpty();
    }

    @Test
    void checkDuplicates_pendingProceedingWithFileNumberAndCourt_shouldDoNothing()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var court =
          databaseCourtRepository.save(
              CourtDTO.builder().type("BFH").isForeignCourt(false).isSuperiorCourt(true).build());
      var dto1 =
          repository.save(
              PendingProceedingDTO.builder()
                  .documentationOffice(documentationOffice)
                  .court(court)
                  .documentNumber("DocumentNumb1")
                  .build());
      var pendingProceedingDTO =
          dto1.toBuilder()
              .fileNumbers(
                  List.of(
                      FileNumberDTO.builder()
                          .rank(0L)
                          .value("123")
                          .documentationUnit(dto1)
                          .build()))
              .build();
      var decision =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .court(CourtTransformer.transformToDomain(court))
                      .deviatingDecisionDates(List.of(LocalDate.of(2023, 12, 12)))
                      .documentNumber("DocumentNumb2")
                      .fileNumbers(List.of("123"))
                      .build()));

      repository.save(pendingProceedingDTO);

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkDuplicates("DocumentNumb2");

      var foundDocUnit = (Decision) documentationUnitService.getByUuid(decision.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).isEmpty();
      assertThat(foundDocUnit.managementData().duplicateRelations()).isEmpty();
    }

    @Test
    void checkDuplicates_pendingProceedingWithDeviatingFileNumberAndCourt_shouldDoNothing()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var court =
          databaseCourtRepository.save(
              CourtDTO.builder().type("BFH").isForeignCourt(false).isSuperiorCourt(true).build());
      var dto1 =
          repository.save(
              PendingProceedingDTO.builder()
                  .documentationOffice(documentationOffice)
                  .court(court)
                  .documentNumber("DocumentNumb1")
                  .build());
      var pendingProceedingDTO =
          dto1.toBuilder()
              .deviatingFileNumbers(
                  List.of(
                      DeviatingFileNumberDTO.builder()
                          .rank(0L)
                          .value("123")
                          .documentationUnit(dto1)
                          .build()))
              .build();
      var decision =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .deviatingFileNumbers(List.of("123"))
                      .documentNumber("DocumentNumb2")
                      .fileNumbers(List.of("123"))
                      .build()));

      repository.save(pendingProceedingDTO);

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkDuplicates("DocumentNumb2");

      var foundDocUnit = (Decision) documentationUnitService.getByUuid(decision.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).isEmpty();
      assertThat(foundDocUnit.managementData().duplicateRelations()).isEmpty();
    }

    @Test
    void checkDuplicates_pendingProceedingWithDeviatingDateAndFileNumber_shouldDoNothing()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var dto1 =
          repository.save(
              PendingProceedingDTO.builder()
                  .documentationOffice(documentationOffice)
                  .deviatingDates(
                      List.of(
                          DeviatingDateDTO.builder()
                              .rank(0L)
                              .value(LocalDate.of(2023, 12, 12))
                              .build()))
                  .documentNumber("DocumentNumb1")
                  .build());
      var pendingProceedingDTO =
          dto1.toBuilder()
              .fileNumbers(
                  List.of(
                      FileNumberDTO.builder()
                          .rank(0L)
                          .value("123")
                          .documentationUnit(dto1)
                          .build()))
              .build();
      var decision =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .deviatingDecisionDates(List.of(LocalDate.of(2023, 12, 12)))
                      .documentNumber("DocumentNumb2")
                      .fileNumbers(List.of("123"))
                      .build()));

      repository.save(pendingProceedingDTO);

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkDuplicates("DocumentNumb2");

      var foundDocUnit = (Decision) documentationUnitService.getByUuid(decision.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).isEmpty();
      assertThat(foundDocUnit.managementData().duplicateRelations()).isEmpty();
    }

    static Stream<Arguments> provideTestCasesWithDuplicates() {
      return Stream.of(
          Arguments.of(
              "findDuplicates_withFileNumberAndDecisionDateAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withFileNumberAndDeviatingDecisionDateAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType2)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .deviatingDecisionDates(List.of(LocalDate.of(2020, 12, 1)))
                  .documentType(documentType2)
                  .build()),
          Arguments.of(
              "findDuplicates_withDeviatingFileNumberAndDecisionDateAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .deviatingFileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withDeviatingFileNumberAndDecisionDateAndDocType_inverse",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .deviatingFileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withDeviatingFileNumberAndDeviatingDecisionDateAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .deviatingFileNumbers(List.of("AZ-123"))
                  .deviatingDecisionDates(List.of(LocalDate.of(2020, 12, 1)))
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withFileNumberAndCourtAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withOneDeviatingFileNumberAndCourtAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .deviatingFileNumbers(List.of("AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withTwoDeviatingFileNumbersAndCourtAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("filenumber1"))
                  .deviatingFileNumbers(List.of("abc", "AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("filenumber2"))
                  .deviatingFileNumbers(List.of("def", "AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withFileNumberAndOneDeviatingCourtAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .deviatingCourts(List.of("Other", "aG aachen"))
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withFileNumberAndTwoDeviatingCourtsAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .deviatingCourts(List.of("OLG Dresden"))
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .deviatingCourts(List.of("OLG Dresden"))
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withTwoDeviatingFileNumbersAndTwoDeviatingCourtsAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .deviatingFileNumbers(List.of("AZ-123"))
                  .deviatingCourts(List.of("OLG Dresden"))
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .deviatingFileNumbers(List.of("AZ-123"))
                  .deviatingCourts(List.of("OLG Dresden"))
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withEcli",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .ecli("ECLI:DE:BFH:2024:B.080980.TEST.00.0")
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .ecli("ECLI:DE:BFH:2024:B.080980.TEST.00.0")
                  .build()),
          Arguments.of(
              "findDuplicates_withDeviatingEcli",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .ecli("ECLI:DE:BFH:2024:B.080980.TEST.00.0")
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .deviatingEclis(List.of("ECLI:DE:BFH:2024:B.080980.TEST.00.0"))
                  .build()));
    }

    static Stream<Arguments> provideTestCasesWithoutDuplicates() {
      return Stream.of(
          Arguments.of(
              "findNoDuplicates_withFileNumberAndDecisionDateAndDifferentDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType2)
                  .build()),
          Arguments.of(
              "findNoDuplicates_withFileNumberAndDifferentDecisionDateAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 2))
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findNoDuplicates_withDifferentFileNumberAndDecisionDateAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("Diff_AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withDifferentFileNumberAndCourtAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-456"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withFileNumberAndDifferentCourtAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .court(courtOlgDresden)
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withFileNumberAndCourtAndDifferentDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType2)
                  .build()),
          Arguments.of(
              "findDuplicates_withDifferentEcli",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .ecli("ECLI:DE:BFH:2024:B.080980.TEST.00.0")
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .ecli("ECLI:DE:BFH:2025:B.080980.TEST.00.0")
                  .build()));
    }
  }

  @Builder(toBuilder = true)
  public record CreationParameters(
      String documentNumber,
      DocumentationOffice documentationOffice,
      Boolean isJdvDuplicateCheckActive,
      List<String> fileNumbers,
      List<String> deviatingFileNumbers,
      LocalDate decisionDate,
      List<LocalDate> deviatingDecisionDates,
      Court court,
      List<String> deviatingCourts,
      DocumentType documentType,
      String ecli,
      List<String> deviatingEclis,
      PublicationStatus publicationStatus) {}

  private DecisionDTO generateNewDocumentationUnit(
      DocumentationOffice userDocOffice, Optional<CreationParameters> parameters)
      throws DocumentationUnitException {

    // default office is user office
    CreationParameters params =
        parameters.orElse(CreationParameters.builder().documentationOffice(userDocOffice).build());
    if (params.documentationOffice() == null) {
      params = params.toBuilder().documentationOffice(userDocOffice).build();
    }

    Decision docUnit =
        Decision.builder()
            .version(0L)
            .documentNumber(params.documentNumber())
            .coreData(
                CoreData.builder()
                    .documentationOffice(params.documentationOffice())
                    .documentType(params.documentType())
                    .decisionDate(params.decisionDate())
                    .deviatingDecisionDates(params.deviatingDecisionDates())
                    .court(params.court())
                    .deviatingCourts(params.deviatingCourts())
                    .ecli(params.ecli())
                    .deviatingEclis(params.deviatingEclis())
                    .build())
            .status(Status.builder().publicationStatus(params.publicationStatus()).build())
            .build();

    var documentationUnitDTO =
        (DecisionDTO)
            repository.save(
                DecisionTransformer.transformToDTO(
                    DecisionDTO.builder()
                        .documentationOffice(
                            DocumentationOfficeTransformer.transformToDTO(
                                docUnit.coreData().documentationOffice()))
                        .creatingDocumentationOffice(
                            DocumentationOfficeTransformer.transformToDTO(
                                docUnit.coreData().creatingDocOffice()))
                        .isJdvDuplicateCheckActive(params.isJdvDuplicateCheckActive())
                        .build(),
                    docUnit));

    if (params.publicationStatus != null) {
      documentationUnitDTO =
          documentationUnitDTO.toBuilder()
              .status(
                  StatusDTO.builder()
                      .publicationStatus(params.publicationStatus())
                      .documentationUnit(documentationUnitDTO)
                      .build())
              .build();
    }

    if (params.fileNumbers() != null) {
      DecisionDTO finalDocumentationUnitDTO = documentationUnitDTO;
      var fileNumbers =
          params.fileNumbers.stream()
              .map(
                  fileNumber ->
                      FileNumberDTO.builder()
                          .documentationUnit(finalDocumentationUnitDTO)
                          .value(fileNumber)
                          .rank(0L)
                          .build())
              .toList();

      documentationUnitDTO = documentationUnitDTO.toBuilder().fileNumbers(fileNumbers).build();
    }

    if (params.deviatingFileNumbers() != null) {
      DecisionDTO finalDocumentationUnitDTO = documentationUnitDTO;
      var devfileNumbers =
          params.deviatingFileNumbers.stream()
              .map(
                  fileNumber ->
                      DeviatingFileNumberDTO.builder()
                          .documentationUnit(finalDocumentationUnitDTO)
                          .value(fileNumber)
                          .rank(0L)
                          .build())
              .toList();

      documentationUnitDTO =
          documentationUnitDTO.toBuilder().deviatingFileNumbers(devfileNumbers).build();
    }

    return repository.save(documentationUnitDTO);
  }
}
