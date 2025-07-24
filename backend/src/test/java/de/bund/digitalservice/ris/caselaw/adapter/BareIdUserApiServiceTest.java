package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
@Import({BareIdUserApiService.class})
class BareIdUserApiServiceTest {

  @Autowired BareIdUserApiService bareIdUserApiService;

  @MockitoBean RestTemplate restTemplate;
  @MockitoBean BareIdUserApiTokenService bareIdUserApiTokenService;

  @BeforeEach
  void setUp() {
    OAuth2AccessToken mockToken =
        new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "mocked-token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600));
    when(bareIdUserApiTokenService.getAccessToken()).thenReturn(mockToken);
  }

  @Test
  void getBareIdToken() {
    var user_id = UUID.randomUUID();
    OidcUser mockOAuth2User = createMockOidcUser(user_id);

    ResponseEntity<OAuth2User> mockResponse = ResponseEntity.ok(mockOAuth2User);
    doReturn(mockResponse)
        .when(restTemplate)
        .exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(OidcUser.class));

    var userResult = bareIdUserApiService.getUser(user_id);

    Assertions.assertEquals("Tina Taxpayer", userResult.name());
    Assertions.assertEquals("e2e_tests_bfh@digitalservice.bund.de", userResult.email());
    Assertions.assertEquals(user_id, userResult.id());
  }

  private OidcUser createMockOidcUser(UUID userId) {
    OidcUserInfo userInfo = Mockito.mock(OidcUserInfo.class);
    when(userInfo.getFullName()).thenReturn("Tina Taxpayer");
    when(userInfo.getEmail()).thenReturn("e2e_tests_bfh@digitalservice.bund.de");

    OidcIdToken idToken = Mockito.mock(OidcIdToken.class);
    when(idToken.toString()).thenReturn(userId.toString());

    OidcUser mockUser = Mockito.mock(OidcUser.class);
    when(mockUser.getUserInfo()).thenReturn(userInfo);
    when(mockUser.getIdToken()).thenReturn(idToken);

    return mockUser;
  }
}
