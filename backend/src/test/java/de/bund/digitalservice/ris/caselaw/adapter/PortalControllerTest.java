package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.bund.digitalservice.ris.caselaw.PortalControllerTestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseApiKeyRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.ProcedureService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = PortalController.class)
@Import({PortalControllerTestConfig.class})
class PortalControllerTest {
  @Autowired private RisWebTestClient risWebClient;
  @MockitoBean private DocumentationUnitService service;
  @MockitoBean private PublicPortalPublicationService portalService;
  @MockitoBean private UserService userService;
  @MockitoBean private ClientRegistrationRepository clientRegistrationRepository;
  @MockitoBean private ProcedureService procedureService;
  @MockitoBean DatabaseApiKeyRepository apiKeyRepository;
  @MockitoBean DatabaseDocumentationOfficeRepository officeRepository;
  @MockitoBean private PatchMapperService patchMapperService;

  @Test
  void testDeleteFromPortal_withInternalUser_shouldSucceed() throws JsonProcessingException {
    doReturn(true).when(userService).isInternal(any());
    risWebClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/portal/ABCD202200001")
        .exchange()
        .expectStatus()
        .is2xxSuccessful();

    verify(portalService, times(1)).deleteDocumentationUnit("ABCD202200001");
    verify(portalService, times(1)).uploadChangelog(new ArrayList<>(), List.of("ABCD202200001"));
  }

  @Test
  void testDeleteFromPortal_withoutInternalUser_shouldFail() throws JsonProcessingException {
    risWebClient
        .withExternalLogin()
        .delete()
        .uri("/api/v1/caselaw/portal/ABCD202200002")
        .exchange()
        .expectStatus()
        .isForbidden(); // Expect 403 when user is unauthorized

    verify(portalService, never()).deleteDocumentationUnit(anyString());
    verify(portalService, never()).uploadChangelog(anyList(), anyList());
  }

  @Test
  void testDeleteFromPortal_whenDocumentDoesNotExist_shouldSucceed()
      throws JsonProcessingException {
    doReturn(true).when(userService).isInternal(any());
    doNothing().when(portalService).deleteDocumentationUnit("NON_EXISTENT_DOCUNIT");
    doNothing().when(portalService).uploadChangelog(anyList(), anyList());

    risWebClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/portal/NON_EXISTENT_DOCUNIT")
        .exchange()
        .expectStatus()
        .isNoContent(); // 204 expected

    verify(portalService, times(1)).deleteDocumentationUnit("NON_EXISTENT_DOCUNIT");
    verify(portalService, times(1))
        .uploadChangelog(new ArrayList<>(), List.of("NON_EXISTENT_DOCUNIT"));
  }

  @Test
  void testDeleteFromPortal_whenDeleteFails_shouldReturn500() throws JsonProcessingException {
    doReturn(true).when(userService).isInternal(any());
    doThrow(new RuntimeException("Deletion error"))
        .when(portalService)
        .deleteDocumentationUnit("ABCD202200001");

    risWebClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/portal/ABCD202200001")
        .exchange()
        .expectStatus()
        .is5xxServerError();

    verify(portalService, times(1)).deleteDocumentationUnit("ABCD202200001");
    verify(portalService, never()).uploadChangelog(anyList(), anyList());
  }

  @Test
  void testDeleteFromPortal_whenUploadChangelogFails_shouldReturn500()
      throws JsonProcessingException {
    doReturn(true).when(userService).isInternal(any());
    doNothing().when(portalService).deleteDocumentationUnit("ABCD202200001");
    doThrow(new RuntimeException("Changelog error"))
        .when(portalService)
        .uploadChangelog(anyList(), anyList());

    risWebClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/portal/ABCD202200001")
        .exchange()
        .expectStatus()
        .is5xxServerError();

    verify(portalService, times(1)).deleteDocumentationUnit("ABCD202200001");
    verify(portalService, times(1)).uploadChangelog(new ArrayList<>(), List.of("ABCD202200001"));
  }
}
