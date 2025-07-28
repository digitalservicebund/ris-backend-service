package de.bund.digitalservice.ris.caselaw.adapter;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BareIdUserApiTokenService {

  private OAuth2AccessToken accessToken;

  private static final String REGISTRATION_ID = "bareid-user-api-client";

  private final OAuth2AuthorizedClientManager authorizedClientManager;

  public BareIdUserApiTokenService(OAuth2AuthorizedClientManager authorizedClientManager) {
    this.authorizedClientManager = authorizedClientManager;
    try {
      setAccessToken();
    } catch (Exception e) {
      log.error("Did not retrieve api token", e);
    }
  }

  public OAuth2AccessToken getAccessToken() {
    if (!isTokenValid(accessToken)) setAccessToken();
    return accessToken;
  }

  private void setAccessToken() {
    accessToken = null;
    OAuth2AuthorizeRequest req =
        OAuth2AuthorizeRequest.withClientRegistrationId(REGISTRATION_ID)
            .principal("neuris")
            .build();
    OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(req);
    if (authorizedClient == null) return;
    accessToken = authorizedClient.getAccessToken();
  }

  private static boolean isTokenValid(OAuth2AccessToken token) {
    return token != null
        && token.getExpiresAt() != null
        && token.getExpiresAt().isAfter(Instant.now());
  }
}
