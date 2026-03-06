package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import de.bund.digitalservice.ris.caselaw.adapter.publication.SyncJob;
import de.bund.digitalservice.ris.caselaw.adapter.publication.sli.SliPassiveCitationSyncJob;
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

class SliPassiveCitationSyncJobIntegrationTest extends BaseIntegrationTest {

  @TestConfiguration
  static class PortalPublicationConfig {

    @Bean
    @Primary
    public PortalTransformer fullLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
      return new FullLdmlTransformer(documentBuilderFactory);
    }
  }

  @Autowired private SliPassiveCitationSyncJob syncJob;

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
  @Sql(scripts = "classpath:sli_ref_view_init.sql")
  @Transactional
  void testRun() {
    TestTransaction.end();
    jobSyncStatusRepository.save(
        new JobSyncStatus(
            SyncJob.SLI_PASSIVE_CITATION_SYNC.getName(), Instant.now().minusSeconds(300)));

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

    Instant beforeSync = Instant.now();

    syncJob.runSync();

    // Updates the sync status
    var statusAfterSync =
        jobSyncStatusRepository.findById(SyncJob.SLI_PASSIVE_CITATION_SYNC.getName());
    assertThat(statusAfterSync).isNotEmpty();
    assertThat(statusAfterSync.get().getLastRun()).isAfter(beforeSync);

    // Updates the passive citations
    TestTransaction.start();
    DecisionDTO updatedDecision =
        (DecisionDTO)
            databaseDocumentationUnitRepository
                .findById(UUID.fromString("adb8408b-5a77-48f9-9ed0-b8dee4f2db02"))
                .orElseThrow();
    assertThat(updatedDecision.getPassiveSliCitations()).hasSize(1);
    TestTransaction.end();

    // Publishes the document with a new passive citation
    ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client, times(2)).putObject(captor.capture(), any(RequestBody.class));

    var capturedRequests = captor.getAllValues();
    assertThat(capturedRequests.get(0).key()).isEqualTo("YYTestDoc2000/YYTestDoc2000.xml");
    assertThat(capturedRequests.get(1).key()).contains("changelogs/");
  }
}
