package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JobSyncStatus;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JobSyncStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalEffectDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.publication.SyncJob;
import de.bund.digitalservice.ris.caselaw.adapter.publication.uli.UliPassiveCitationSyncJob;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.FullLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.PortalTransformer;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.transaction.TestTransaction;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Sql(scripts = {"classpath:uli_ref_view_init.sql"})
@Sql(
    scripts = {"classpath:uli_ref_view_cleanup.sql"},
    executionPhase = AFTER_TEST_METHOD)
class UliPassiveCitationSyncJobIntegrationTest extends BaseIntegrationTest {

  @TestConfiguration
  static class PortalPublicationConfig {

    @Bean
    @Primary
    public PortalTransformer fullLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
      return new FullLdmlTransformer(documentBuilderFactory);
    }
  }

  @Autowired private UliPassiveCitationSyncJob syncJob;

  @Autowired private JobSyncStatusRepository jobSyncStatusRepository;
  @Autowired private DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;
  @Autowired private DatabaseCourtRepository databaseCourtRepository;
  @Autowired private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;

  @MockitoBean(name = "portalS3Client")
  private S3Client s3Client;

  @BeforeEach
  void setUp() {
    when(featureToggleService.isEnabled("neuris.portal-publication")).thenReturn(true);
    when(featureToggleService.isEnabled("neuris.regular-changelogs")).thenReturn(true);
  }

  @Test
  @Transactional
  void handleNewlyPublishedAfter_shouldUpdateExistingAndCreateMissingPassiveCitations() {
    TestTransaction.end();
    jobSyncStatusRepository.save(
        new JobSyncStatus(
            SyncJob.ULI_PASSIVE_CITATION_SYNC.getName(), Instant.now().minusSeconds(300)));

    CourtDTO court =
        databaseCourtRepository.saveAndFlush(
            CourtDTO.builder()
                .type("AG")
                .location("Aachen")
                .isSuperiorCourt(false)
                .isForeignCourt(false)
                .jurisId(new Random().nextInt())
                .build());
    var docType =
        databaseDocumentTypeRepository.saveAndFlush(
            DocumentTypeDTO.builder().abbreviation("test").multiple(true).build());

    DecisionDTO decision =
        (DecisionDTO)
            databaseDocumentationUnitRepository
                .findById(UUID.fromString("adb8408b-5a77-48f9-9ed0-b8dee4f2db02"))
                .orElseThrow();
    decision.setDocumentType(docType);
    decision.setCourt(court);
    decision.setDate(LocalDate.now());
    decision.setFileNumbers(
        List.of(FileNumberDTO.builder().documentationUnit(decision).value("123").rank(0L).build()));
    decision.setGrounds("lorem ipsum dolor sit amet");
    decision.setLegalEffect(LegalEffectDTO.JA);
    UUID caselawId = UUID.fromString("adb8408b-5a77-48f9-9ed0-b8dee4f2db02");
    UUID uliIdToUpdate = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    decision.setPassiveUliCitations(
        List.of(
            PassiveCitationUliDTO.builder()
                .sourceId(uliIdToUpdate)
                .sourceAuthor("Old Author")
                .sourceCitation("Old Citation")
                .target(decision)
                .rank(1)
                .build()));
    databaseDocumentationUnitRepository.save(decision);

    syncJob.runSync();

    TestTransaction.start();
    DecisionDTO updatedDecision =
        (DecisionDTO) databaseDocumentationUnitRepository.findById(caselawId).orElseThrow();

    // We expect 2, the updated and the new citation
    assertThat(updatedDecision.getPassiveUliCitations()).hasSize(2);

    // 1. Check: Update
    PassiveCitationUliDTO updated =
        updatedDecision.getPassiveUliCitations().stream()
            .filter(p -> p.getSourceId().equals(uliIdToUpdate))
            .findFirst()
            .orElseThrow();
    assertThat(updated.getSourceAuthor()).isEqualTo("ULI author"); // from the first uli document
    assertThat(updated.getSourceCitation())
        .isEqualTo("ULI citation"); // from the first uli document

    // 2. Check: Create Missing
    UUID uliIdToCreate =
        UUID.fromString("661f9511-f30c-52e5-b827-557766551111"); // the second uli document
    PassiveCitationUliDTO created =
        updatedDecision.getPassiveUliCitations().stream()
            .filter(p -> uliIdToCreate.equals(p.getSourceId()))
            .findFirst()
            .orElseThrow();
    assertThat(created.getSourceAuthor()).isEqualTo("Second ULI author");
    assertThat(created.getRank()).isEqualTo(2);

    TestTransaction.end();

    verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  @Transactional
  void handleNewlyPublishedAfter_doesNotAddNewPassiveCitationIfItAlreadyExists() {
    TestTransaction.end();
    jobSyncStatusRepository.save(
        new JobSyncStatus(
            SyncJob.ULI_PASSIVE_CITATION_SYNC.getName(), Instant.now().minusSeconds(300)));

    CourtDTO court =
        databaseCourtRepository.saveAndFlush(
            CourtDTO.builder()
                .type("AG")
                .location("Aachen")
                .isSuperiorCourt(false)
                .isForeignCourt(false)
                .jurisId(new Random().nextInt())
                .build());
    var docType =
        databaseDocumentTypeRepository.saveAndFlush(
            DocumentTypeDTO.builder().abbreviation("test").multiple(true).build());

    DecisionDTO decision =
        (DecisionDTO)
            databaseDocumentationUnitRepository
                .findById(UUID.fromString("adb8408b-5a77-48f9-9ed0-b8dee4f2db02"))
                .orElseThrow();
    decision.setDocumentType(docType);
    decision.setCourt(court);
    decision.setDate(LocalDate.now());
    decision.setFileNumbers(
        List.of(FileNumberDTO.builder().documentationUnit(decision).value("123").rank(0L).build()));
    decision.setGrounds("lorem ipsum dolor sit amet");
    decision.setLegalEffect(LegalEffectDTO.JA);
    decision.setPassiveUliCitations(
        List.of(
            PassiveCitationUliDTO.builder()
                .sourceId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .sourceAuthor("ULI author")
                .sourceCitation("ULI citation")
                .sourceLiteratureDocumentNumber("ULI-TEST-1")
                .target(decision)
                .rank(1)
                .build(),
            PassiveCitationUliDTO.builder()
                .sourceId(UUID.fromString("661f9511-f30c-52e5-b827-557766551111"))
                .sourceAuthor("Second ULI author")
                .sourceCitation("Second citation")
                .sourceLiteratureDocumentNumber("ULI-TEST-2")
                .target(decision)
                .rank(2)
                .build()));
    databaseDocumentationUnitRepository.save(decision);

    syncJob.runSync();

    // The passive citations are still there
    TestTransaction.start();
    DecisionDTO updatedDecision =
        (DecisionDTO)
            databaseDocumentationUnitRepository
                .findById(UUID.fromString("adb8408b-5a77-48f9-9ed0-b8dee4f2db02"))
                .orElseThrow();
    assertThat(updatedDecision.getPassiveUliCitations()).hasSize(2);
    TestTransaction.end();

    // Does not publish anything
    verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  @Transactional
  void handleNewlyPublishedAfter_shouldNotProcessAlreadySynchronizedDocumentsAgain() {
    TestTransaction.end();
    jobSyncStatusRepository.save(
        new JobSyncStatus(
            SyncJob.ULI_PASSIVE_CITATION_SYNC.getName(), Instant.now().minusSeconds(300)));

    CourtDTO court =
        databaseCourtRepository.saveAndFlush(
            CourtDTO.builder()
                .type("AG")
                .location("Aachen")
                .isSuperiorCourt(false)
                .isForeignCourt(false)
                .jurisId(new Random().nextInt())
                .build());
    var docType =
        databaseDocumentTypeRepository.saveAndFlush(
            DocumentTypeDTO.builder().abbreviation("test").multiple(true).build());

    DecisionDTO decision =
        (DecisionDTO)
            databaseDocumentationUnitRepository
                .findById(UUID.fromString("adb8408b-5a77-48f9-9ed0-b8dee4f2db02"))
                .orElseThrow();
    decision.setDocumentType(docType);
    decision.setCourt(court);
    decision.setDate(LocalDate.now());
    decision.setFileNumbers(
        List.of(FileNumberDTO.builder().documentationUnit(decision).value("123").rank(0L).build()));
    decision.setGrounds("lorem ipsum dolor sit amet");
    decision.setLegalEffect(LegalEffectDTO.JA);
    databaseDocumentationUnitRepository.save(decision);

    syncJob.runSync();
    var afterSyncOne = Instant.now();
    syncJob.runSync();

    // Updates the sync status
    var statusAfterSync =
        jobSyncStatusRepository.findById(SyncJob.ULI_PASSIVE_CITATION_SYNC.getName());
    assertThat(statusAfterSync).isNotEmpty();
    assertThat(statusAfterSync.get().getLastRun()).isAfter(afterSyncOne);

    // The passive citations are updated
    TestTransaction.start();
    DecisionDTO updatedDecision =
        (DecisionDTO)
            databaseDocumentationUnitRepository
                .findById(UUID.fromString("adb8408b-5a77-48f9-9ed0-b8dee4f2db02"))
                .orElseThrow();
    assertThat(updatedDecision.getPassiveUliCitations()).hasSize(2);
    TestTransaction.end();

    // Publishes the document with a new passive citation, but only once
    ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client, times(2)).putObject(captor.capture(), any(RequestBody.class));

    var capturedRequests = captor.getAllValues();
    assertThat(capturedRequests.get(0).key()).isEqualTo("YYTestDoc2000/YYTestDoc2000.xml");
    assertThat(capturedRequests.get(1).key()).contains("changelogs/");
  }
}
