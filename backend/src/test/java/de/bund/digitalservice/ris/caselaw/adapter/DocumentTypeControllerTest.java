package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.DocumentTypeCategory;
import de.bund.digitalservice.ris.caselaw.domain.DocumentTypeService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DocumentTypeController.class)
@Import({SecurityConfig.class, TestConfig.class})
class DocumentTypeControllerTest {

  @Autowired private RisWebTestClient risWebTestClient;

  @MockitoBean private DocumentTypeService service;
  @MockitoBean private ClientRegistrationRepository clientRegistrationRepository;

  private DocumentType documentType1;
  private DocumentType documentType2;

  @BeforeEach
  void setUp() {
    documentType1 =
        DocumentType.builder()
            .uuid(UUID.randomUUID())
            .label("label1")
            .jurisShortcut("abbreviation1")
            .build();
    documentType2 =
        DocumentType.builder()
            .uuid(UUID.randomUUID())
            .label("label2")
            .jurisShortcut("abbreviation2")
            .build();
  }

  @Test
  void shouldReturnListOfDocumentTypes_whenGetDocumentTypesIsCalled() {
    List<DocumentType> documentTypes = List.of(documentType1, documentType2);

    when(service.getDocumentTypes(any(Optional.class), eq(DocumentTypeCategory.CASELAW)))
        .thenReturn(documentTypes);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documenttypes?category=CASELAW")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1))
        .getDocumentTypes(any(Optional.class), eq(DocumentTypeCategory.CASELAW));
  }

  @Test
  void shouldCallServiceWithSearchQueryParameter_whenGetDocumentTypesIsCalledWithSearchString() {
    List<DocumentType> documentTypes = List.of(documentType1, documentType2);

    when(service.getDocumentTypes(any(Optional.class), eq(DocumentTypeCategory.CASELAW)))
        .thenReturn(documentTypes);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documenttypes?q=label1&category=CASELAW")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).getDocumentTypes(Optional.of("label1"), DocumentTypeCategory.CASELAW);
  }
}
