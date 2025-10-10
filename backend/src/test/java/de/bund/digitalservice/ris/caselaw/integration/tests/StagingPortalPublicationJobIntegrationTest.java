package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationJobStatus.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.adapter.PortalPublicationJobService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionNameDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalEffectDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PortalPublicationJobDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PortalPublicationJobRepository;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.FullLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.PublicationJobStatus;
import de.bund.digitalservice.ris.caselaw.domain.PublicationJobType;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Import({StagingPortalPublicationJobIntegrationTest.PortalPublicationConfig.class})
class StagingPortalPublicationJobIntegrationTest extends BaseIntegrationTest {

  @TestConfiguration
  static class PortalPublicationConfig {

    @Bean
    @Primary
    public de.bund.digitalservice.ris.caselaw.adapter.PortalTransformer fullLdmlTransformer(
        DocumentBuilderFactory documentBuilderFactory) {
      return new FullLdmlTransformer(documentBuilderFactory);
    }
  }

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private PortalPublicationJobRepository portalPublicationJobRepository;

  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseCourtRepository databaseCourtRepository;
  @Autowired private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  @Autowired private PortalPublicationJobService portalPublicationJobService;

  @MockitoBean(name = "portalS3Client")
  private S3Client s3Client;

  private final DocumentationOffice docOffice = buildDSDocOffice();
  private DocumentationOfficeDTO documentationOffice;

  @BeforeEach
  void setUp() {
    documentationOffice =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());
    portalPublicationJobRepository.deleteAll();
    when(featureToggleService.isEnabled("neuris.portal-publication")).thenReturn(true);
    when(featureToggleService.isEnabled("neuris.regular-changelogs")).thenReturn(true);
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void shouldPublishWithAllowedStagingData() throws IOException {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSaveDecision(repository, buildValidDocumentationUnit("1"));
    ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

    portalPublicationJobRepository.saveAll(
        List.of(createPublicationJob(dto, PublicationJobType.PUBLISH)));

    portalPublicationJobService.executePendingJobs();

    verify(s3Client, times(2)).putObject(putCaptor.capture(), bodyCaptor.capture());

    var fileNameRequests = putCaptor.getAllValues();
    var bodyRequests = bodyCaptor.getAllValues();
    var ldmlContent =
        new String(
            bodyRequests.getFirst().contentStreamProvider().newStream().readAllBytes(),
            StandardCharsets.UTF_8);
    var changelogContent =
        new String(
            bodyRequests.get(1).contentStreamProvider().newStream().readAllBytes(),
            StandardCharsets.UTF_8);

    assertThat(fileNameRequests.getFirst().key())
        .isEqualTo(dto.getDocumentNumber() + "/" + dto.getDocumentNumber() + ".xml");
    assertThat(ldmlContent)
        .contains("gruende test")
        .contains("entscheidungsname test")
        .contains("orientierungssatz test");
    assertThat(fileNameRequests.get(1).key()).contains("changelog");
    assertThat(changelogContent)
        .isEqualTo(
            """
                    {"changed":["1/1.xml"],"deleted":[]}""");
  }

  @Test
  void shouldOnlyAddDocumentNumberToChangelogForLatestKindOfJob() throws IOException {
    DocumentationUnitDTO dto1 =
        EntityBuilderTestUtil.createAndSaveDecision(repository, buildValidDocumentationUnit("1"));
    DocumentationUnitDTO dto2 =
        EntityBuilderTestUtil.createAndSaveDecision(repository, buildValidDocumentationUnit("2"));

    portalPublicationJobRepository.saveAll(
        List.of(
            createPublicationJob(dto1, PublicationJobType.PUBLISH),
            createPublicationJob(dto2, PublicationJobType.DELETE),
            createPublicationJob(dto1, PublicationJobType.PUBLISH),
            createPublicationJob(dto2, PublicationJobType.PUBLISH),
            createPublicationJob(dto1, PublicationJobType.DELETE),
            createPublicationJob(dto2, PublicationJobType.PUBLISH)));

    when(s3Client.listObjectsV2(
            ListObjectsV2Request.builder().bucket("no-bucket").prefix("1/").build()))
        .thenReturn(
            ListObjectsV2Response.builder()
                .contents(S3Object.builder().key("1/1.xml").build())
                .build());

    when(s3Client.listObjectsV2(
            ListObjectsV2Request.builder().bucket("no-bucket").prefix("2/").build()))
        .thenReturn(
            ListObjectsV2Response.builder()
                .contents(S3Object.builder().key("2/2.xml").build())
                .build());

    portalPublicationJobService.executePendingJobs();

    ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
    ArgumentCaptor<DeleteObjectRequest> deleteCaptor =
        ArgumentCaptor.forClass(DeleteObjectRequest.class);

    // TWO DELETE JOBS
    verify(s3Client, times(2)).deleteObject(deleteCaptor.capture());
    // PUT 4 PUBLISH JOBS + PUT changelog
    verify(s3Client, times(5)).putObject(putCaptor.capture(), bodyCaptor.capture());

    var capturedPutRequests = putCaptor.getAllValues();
    var capturedDeleteRequests = deleteCaptor.getAllValues();
    var changelogContent =
        new String(
            bodyCaptor.getAllValues().get(4).contentStreamProvider().newStream().readAllBytes(),
            StandardCharsets.UTF_8);

    assertThat(capturedPutRequests.get(0).key()).isEqualTo("1/1.xml");
    assertThat(capturedPutRequests.get(1).key()).isEqualTo("1/1.xml");
    assertThat(capturedPutRequests.get(2).key()).isEqualTo("2/2.xml");
    assertThat(capturedPutRequests.get(3).key()).isEqualTo("2/2.xml");
    assertThat(capturedPutRequests.get(4).key()).contains("changelogs/");
    assertThat(capturedDeleteRequests.get(0).key()).isEqualTo("2/2.xml");
    assertThat(capturedDeleteRequests.get(1).key()).isEqualTo("1/1.xml");
    //         ensure that each document number only appears either in changed or deleted section
    assertThat(changelogContent)
        .isEqualTo(
            """
                          {"changed":["2/2.xml"],"deleted":["1/1.xml"]}""");

    assertThat(portalPublicationJobRepository.findAll())
        .allMatch(job -> job.getPublicationJobStatus() == SUCCESS);
  }

  @Test
  void shouldContinueExecutionOnError() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSaveDecision(repository, buildValidDocumentationUnit("1"));
    DocumentationUnitDTO dto2 =
        EntityBuilderTestUtil.createAndSaveDecision(repository, buildValidDocumentationUnit("2"));

    // PUBLISH job and upload changelog will fail
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenThrow(new RuntimeException("error"));

    portalPublicationJobRepository.saveAll(
        List.of(
            createPublicationJob(dto, PublicationJobType.PUBLISH),
            createPublicationJob(dto2, PublicationJobType.DELETE)));

    when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
        .thenReturn(
            ListObjectsV2Response.builder()
                .contents(S3Object.builder().key("1/1.xml").build())
                .build());

    portalPublicationJobService.executePendingJobs();

    // DELETE is called even after fail
    verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    // PUT 1.xml (fails) + PUT changelog
    verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));

    assertThat(
            portalPublicationJobRepository.findAll().stream()
                .map(PortalPublicationJobDTO::getPublicationJobStatus)
                .toList())
        .isEqualTo(List.of(PublicationJobStatus.ERROR, SUCCESS));
  }

  @Test
  // In Migration there is no filter for "pure"-deletion jobs (Rsp_Loeschungen.csv), hence we will
  // receive deletion jobs for non-existing documents -> we ignore them by marking them as success
  void executePendingJobs_withFailedDeletionJob_shouldMarkJobAsSuccess() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSaveDecision(repository, buildValidDocumentationUnit("1"));

    // DELETE job will fail as the file is missing
    doThrow(NoSuchKeyException.class).when(s3Client).deleteObject(any(Consumer.class));

    portalPublicationJobRepository.saveAll(
        List.of(createPublicationJob(dto, PublicationJobType.DELETE)));

    portalPublicationJobService.executePendingJobs();

    assertThat(
            portalPublicationJobRepository.findAll().stream()
                .map(PortalPublicationJobDTO::getPublicationJobStatus)
                .toList())
        .hasSize(1)
        .isEqualTo(List.of(SUCCESS));
  }

  private PortalPublicationJobDTO createPublicationJob(
      DocumentationUnitDTO dto, PublicationJobType publicationType) {
    return PortalPublicationJobDTO.builder()
        .documentNumber(dto.getDocumentNumber())
        .createdAt(Instant.now())
        .publicationJobStatus(PublicationJobStatus.PENDING)
        .publicationJobType(publicationType)
        .build();
  }

  private DecisionDTO.DecisionDTOBuilder<?, ?> buildValidDocumentationUnit(String docNumber) {
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

    return DecisionDTO.builder()
        .documentNumber(docNumber)
        .documentType(docType)
        .documentationOffice(documentationOffice)
        .court(court)
        .date(LocalDate.now())
        .legalEffect(LegalEffectDTO.JA)
        .fileNumbers(List.of(FileNumberDTO.builder().value("123").rank(0L).build()))
        .grounds("gruende test")
        .headnote("orientierungssatz test")
        .decisionNames(
            List.of(DecisionNameDTO.builder().value("entscheidungsname test").rank(1).build()));
  }
}
