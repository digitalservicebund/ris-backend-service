package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
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
  void testGetUser() {
    final UUID userId = UUID.randomUUID();

    BareUserApiResponse.BareUser bareUser = generateBareUser(userId);
    BareUserApiResponse.UserApiResponse userApiResponse =
        new BareUserApiResponse.UserApiResponse(bareUser);

    ResponseEntity<BareUserApiResponse.UserApiResponse> mockResponse =
        ResponseEntity.ok(userApiResponse);

    doReturn(mockResponse)
        .when(restTemplate)
        .exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(BareUserApiResponse.UserApiResponse.class));

    var userResult = bareIdUserApiService.getUser(userId);

    Assertions.assertEquals("Tina Taxpayer", userResult.name());
    Assertions.assertEquals("e2e_tests_bfh@digitalservice.bund.de", userResult.email());
    Assertions.assertEquals(userId, userResult.id());
  }

  @Test
  void testGetUser_whenApiReturnsBadRequest_shouldReturnUserWithGivenId() {
    final UUID userId = UUID.randomUUID();

    ResponseEntity<BareUserApiResponse.UserApiResponse> mockResponse =
        ResponseEntity.badRequest().build();

    doReturn(mockResponse)
        .when(restTemplate)
        .exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(BareUserApiResponse.UserApiResponse.class));

    var userResult = bareIdUserApiService.getUser(userId);

    Assertions.assertEquals(userId, userResult.id());
    Assertions.assertNull(userResult.name());
  }

  private BareUserApiResponse.BareUser generateBareUser(UUID userId) {
    return new BareUserApiResponse.BareUser(
        userId,
        true,
        true,
        "e2e_tests_bfh@digitalservice.bund.de",
        "e2e_tests_bfh@digitalservice.bund.de",
        Map.of(
            "firstName", new BareUserApiResponse.AttributeValues(List.of("Tina")),
            "lastName", new BareUserApiResponse.AttributeValues(List.of("Taxpayer"))));
  }
}
