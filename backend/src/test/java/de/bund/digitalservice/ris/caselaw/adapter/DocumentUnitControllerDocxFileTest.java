package de.bund.digitalservice.ris.caselaw.adapter;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDefaultDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OriginalFileDocumentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReportRepository;
import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = DocumentUnitController.class)
@Import({
  SecurityConfig.class,
  AuthService.class,
  PostgresDocumentationUnitRepositoryImpl.class,
  PostgresJPAConfig.class,
  TestConfig.class
})
class DocumentUnitControllerDocxFileTest {
  @Autowired private RisWebTestClient risWebClient;
  @SpyBean private DocumentUnitService service;
  @MockBean private KeycloakUserService userService;
  @MockBean private DocxConverterService docxConverterService;
  @MockBean private ReactiveClientRegistrationRepository clientRegistrationRepository;
  @MockBean private DatabaseDocumentationUnitRepository repository;
  @MockBean private DocumentNumberService numberService;
  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean private DocumentUnitStatusService statusService;
  @MockBean private PublicationReportRepository reportRepository;

  @Captor private ArgumentCaptor<ByteBuffer> captor;

  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private final DocumentationOffice docOffice = buildDefaultDocOffice();

  @BeforeEach
  void setup() {
    doReturn(Mono.just(docOffice))
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals("/DS");
                }));

    when(service.getByUuid(TEST_UUID))
        .thenReturn(
            Mono.just(
                DocumentUnit.builder()
                    .coreData(CoreData.builder().documentationOffice(docOffice).build())
                    .build()));
  }

  @Test
  void testAttachFileToDocumentUnit() {
    var headersCaptor = ArgumentCaptor.forClass(HttpHeaders.class);
    doReturn(Mono.empty())
        .when(service)
        .attachFileToDocumentUnit(eq(TEST_UUID), any(ByteBuffer.class), any(HttpHeaders.class));

    risWebClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/file")
        .body(BodyInserters.fromValue(new byte[] {}))
        .exchange()
        .expectStatus()
        .isOk();

    verify(service)
        .attachFileToDocumentUnit(eq(TEST_UUID), captor.capture(), headersCaptor.capture());
    assertThat(captor.getValue().array()).isEmpty();
    assertThat(headersCaptor.getValue().getContentLength()).isZero();
  }

  @Test
  void testAttachFileToDocumentUnit_withInvalidUuid() {
    risWebClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/abc/file")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  @Test
  void
      testAttachFileToDocumentationUnit_withECLIInFooter_shouldExtractECLIAndSetItInUnitIfNotSet() {
    Docx2Html docx2Html = new Docx2Html("html", List.of("ecli"));
    DocumentationUnitDTO documentationUnitDTO = new DocumentationUnitDTO();
    documentationUnitDTO.setOriginalFileDocument(
        OriginalFileDocumentDTO.builder().s3ObjectPath("filename").build());

    doReturn(Mono.just(DocumentUnit.builder().s3path("filename").build()))
        .when(service)
        .attachFileToDocumentUnit(eq(TEST_UUID), any(ByteBuffer.class), any(HttpHeaders.class));
    when(docxConverterService.getConvertedObject("filename")).thenReturn(Mono.just(docx2Html));
    when(repository.findById(TEST_UUID)).thenReturn(Optional.of(documentationUnitDTO));

    risWebClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/file")
        .body(BodyInserters.fromValue(new byte[] {}))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Docx2Html.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().ecliList()).containsExactly("ecli");
            });

    verify(service).updateECLI(TEST_UUID, docx2Html);

    documentationUnitDTO.setEcli("ecli");
    verify(repository).save(documentationUnitDTO);
  }

  @Test
  void
      testAttachFileToDocumentationUnit_withECLIInFooter_shouldExtractECLIAndNotChangeTheECLIInUnitIfECLIIsSet() {
    Docx2Html docx2Html = new Docx2Html("html", List.of("ecli"));
    DocumentationUnitDTO documentationUnitDTO = new DocumentationUnitDTO();
    documentationUnitDTO.setOriginalFileDocument(
        OriginalFileDocumentDTO.builder().s3ObjectPath("filename").build());
    documentationUnitDTO.setEcli("oldEcli");

    doReturn(Mono.just(DocumentUnit.builder().s3path("filename").build()))
        .when(service)
        .attachFileToDocumentUnit(eq(TEST_UUID), any(ByteBuffer.class), any(HttpHeaders.class));
    when(docxConverterService.getConvertedObject("filename")).thenReturn(Mono.just(docx2Html));
    when(repository.findById(TEST_UUID)).thenReturn(Optional.of(documentationUnitDTO));

    risWebClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/file")
        .body(BodyInserters.fromValue(new byte[] {}))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Docx2Html.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().ecliList()).containsExactly("ecli");
            });

    verify(service).updateECLI(TEST_UUID, docx2Html);
    verify(repository, never()).save(documentationUnitDTO);
  }

  @Test
  void testRemoveFileFromDocumentUnit() {
    when(service.removeFileFromDocumentUnit(TEST_UUID)).thenReturn(Mono.empty());

    risWebClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/documentunits/" + TEST_UUID + "/file")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).removeFileFromDocumentUnit(TEST_UUID);
  }

  @Test
  void testRemoveFileFromDocumentUnit_withInvalidUuid() {
    risWebClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/documentunits/abc/file")
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }
}
