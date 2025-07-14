package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.adapter.CaselawExceptionHandler;
import de.bund.digitalservice.ris.caselaw.adapter.PortalBucket;
import de.bund.digitalservice.ris.caselaw.adapter.PortalPublicationService;
import de.bund.digitalservice.ris.caselaw.adapter.StagingPortalPublicationService;
import de.bund.digitalservice.ris.caselaw.adapter.XmlUtilService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalEffectDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.FullLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Import({StagingPortalPublicationServiceIntegrationTest.PortalPublicationConfig.class})
class StagingPortalPublicationServiceIntegrationTest extends BaseIntegrationTest {

  @TestConfiguration
  static class PortalPublicationConfig {
    @Bean
    @Primary
    public PortalPublicationService stagingPortalPublicationService(
        DocumentationUnitRepository documentationUnitRepository,
        AttachmentRepository attachmentRepository,
        XmlUtilService xmlUtilService,
        PortalBucket portalBucket,
        ObjectMapper objectMapper,
        de.bund.digitalservice.ris.caselaw.adapter.PortalTransformer portalTransformer) {
      return new StagingPortalPublicationService(
          documentationUnitRepository,
          attachmentRepository,
          xmlUtilService,
          portalBucket,
          objectMapper,
          portalTransformer);
    }

    @Bean
    @Primary
    public de.bund.digitalservice.ris.caselaw.adapter.PortalTransformer stagingPortalTransformer(
        DocumentBuilderFactory documentBuilderFactory) {
      return new FullLdmlTransformer(documentBuilderFactory);
    }
  }

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseCourtRepository databaseCourtRepository;
  @Autowired private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;

  @MockitoBean(name = "portalS3Client")
  private S3Client s3Client;

  private final DocumentationOffice docOffice = buildDSDocOffice();
  private DocumentationOfficeDTO documentationOffice;
  private static final String DEFAULT_DOCUMENT_NUMBER = "1234567890123";

  @BeforeEach
  void setUp() {
    documentationOffice =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void testPublishSuccessfully() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSaveDecision(repository, buildValidDocumentationUnit());

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/publish")
        .exchange()
        .expectStatus()
        .isOk();

    ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);

    verify(s3Client, times(2)).putObject(captor.capture(), any(RequestBody.class));

    var capturedRequests = captor.getAllValues();
    assertThat(capturedRequests.get(0).key()).isEqualTo("1234567890123/1234567890123.xml");
    assertThat(capturedRequests.get(1).key()).contains("changelogs/");
  }

  @Test
  void testPublishFailsWithMissingMandatoryFields() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSaveDecision(repository, documentationOffice);

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/publish")
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(CaselawExceptionHandler.ApiError.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().message())
                    .contains("LDML validation failed."));
  }

  @Test
  void testPublishFailsWhenLDMLValidationFails() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository, buildValidDocumentationUnit().grounds(null));

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/publish")
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(CaselawExceptionHandler.ApiError.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().message())
                    .contains("Missing judgment body."));
  }

  @Test
  void testPublishFailsWhenS3ClientThrowsException() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSaveDecision(repository, buildValidDocumentationUnit());

    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenThrow(S3Exception.class);

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/publish")
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(CaselawExceptionHandler.ApiError.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().message())
                    .contains("Could not save LDML to bucket."));
  }

  @Test
  void testPublishWithAttachmentsSuccessfully() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository,
            buildValidDocumentationUnit(
                List.of(
                    AttachmentDTO.builder()
                        .content(new byte[] {1, 2, 3})
                        .filename("bild1.png")
                        .format("png")
                        .uploadTimestamp(Instant.now())
                        .build())));

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/publish")
        .exchange()
        .expectStatus()
        .isOk();

    ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);

    verify(s3Client, times(3)).putObject(captor.capture(), any(RequestBody.class));

    var capturedRequests = captor.getAllValues();
    assertThat(capturedRequests.get(0).key()).isEqualTo("1234567890123/1234567890123.xml");
    assertThat(capturedRequests.get(1).key()).isEqualTo("1234567890123/bild1.png");
    assertThat(capturedRequests.get(2).key()).contains("changelogs/");
  }

  @Test
  void publishTwice_andRemoveAttachment_shouldPublishSuccessfullyAndDeleteAttachment()
      throws IOException {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository,
            buildValidDocumentationUnit(
                List.of(
                    AttachmentDTO.builder()
                        .content(new byte[] {1, 2, 3})
                        .filename("bild1.png")
                        .format("png")
                        .uploadTimestamp(Instant.now())
                        .build(),
                    AttachmentDTO.builder()
                        .content(new byte[] {1, 2, 3})
                        .filename("bild2.png")
                        .format("png")
                        .uploadTimestamp(Instant.now())
                        .build())));

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/publish")
        .exchange()
        .expectStatus()
        .isOk();

    ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);

    verify(s3Client, times(4)).putObject(captor.capture(), any(RequestBody.class));

    var capturedRequests = captor.getAllValues();
    assertThat(capturedRequests.get(0).key()).isEqualTo("1234567890123/1234567890123.xml");
    assertThat(capturedRequests.get(1).key()).isEqualTo("1234567890123/bild1.png");
    assertThat(capturedRequests.get(2).key()).isEqualTo("1234567890123/bild2.png");
    assertThat(capturedRequests.get(3).key()).contains("changelogs/");

    dto.setAttachments(
        List.of(
            AttachmentDTO.builder()
                .content(new byte[] {1, 2, 3})
                .filename("bild1.png")
                .format("png")
                .uploadTimestamp(Instant.now())
                .documentationUnit(dto)
                .build()));
    dto = repository.save(dto);

    var response =
        ListObjectsV2Response.builder()
            .contents(
                S3Object.builder().key("1234567890123/1234567890123.xml").build(),
                S3Object.builder().key("1234567890123/bild1.png").build(),
                S3Object.builder().key("1234567890123/bild2.png").build())
            .build();

    ListObjectsV2Request request =
        ListObjectsV2Request.builder().bucket("no-bucket").prefix("1234567890123/").build();
    when(s3Client.listObjectsV2(request)).thenReturn(response);

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId() + "/publish")
        .exchange()
        .expectStatus()
        .isOk();

    ArgumentCaptor<PutObjectRequest> updateCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    ArgumentCaptor<DeleteObjectRequest> deleteCaptor =
        ArgumentCaptor.forClass(DeleteObjectRequest.class);
    ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

    verify(s3Client, times(7)).putObject(updateCaptor.capture(), bodyCaptor.capture());
    verify(s3Client, times(1)).deleteObject(deleteCaptor.capture());

    var changelogContent =
        new String(
            bodyCaptor.getAllValues().get(6).contentStreamProvider().newStream().readAllBytes(),
            StandardCharsets.UTF_8);

    var updateCapturedRequests = updateCaptor.getAllValues();
    var deleteCapturedRequests = deleteCaptor.getAllValues();
    assertThat(updateCapturedRequests.get(4).key()).isEqualTo("1234567890123/1234567890123.xml");
    assertThat(updateCapturedRequests.get(5).key()).isEqualTo("1234567890123/bild1.png");
    assertThat(updateCapturedRequests.get(6).key()).contains("changelogs/");
    assertThat(deleteCapturedRequests.get(0).key()).isEqualTo("1234567890123/bild2.png");
    assertThat(changelogContent)
        .isEqualTo(
            """
     {"changed":["1234567890123/1234567890123.xml","1234567890123/bild1.png"],"deleted":["1234567890123/bild2.png"]}""");
  }

  private DecisionDTO.DecisionDTOBuilder<?, ?> buildValidDocumentationUnit() {
    return buildValidDocumentationUnit(Collections.emptyList());
  }

  private DecisionDTO.DecisionDTOBuilder<?, ?> buildValidDocumentationUnit(
      List<AttachmentDTO> attachments) {
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
        .documentNumber(DEFAULT_DOCUMENT_NUMBER)
        .documentType(docType)
        .documentationOffice(documentationOffice)
        .court(court)
        .date(LocalDate.now())
        .legalEffect(LegalEffectDTO.JA)
        .fileNumbers(List.of(FileNumberDTO.builder().value("123").rank(0L).build()))
        .attachments(attachments)
        .grounds("lorem ipsum dolor sit amet");
  }
}
