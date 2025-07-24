package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.UserTransformer;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserApiService;
import java.util.UUID;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class BareIdUserApiService implements UserApiService {

  private final BareIdUserApiTokenService bareIdUserApiTokenService;

  private final RestTemplate restTemplate;

  private final String INSTANCE_ID = "random_id_to_be_added_later";

  public BareIdUserApiService(
      BareIdUserApiTokenService bareIdUserApiTokenService, RestTemplate restTemplate) {
    this.bareIdUserApiTokenService = bareIdUserApiTokenService;
    this.restTemplate = restTemplate;
  }

  @Override
  public User getUser(UUID userId) {

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(bareIdUserApiTokenService.getAccessToken().getTokenValue());
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

    String url = String.format("https://api.bare.id/user/v1/%s/users/%s", INSTANCE_ID, userId);

    ResponseEntity<OidcUser> response =
        restTemplate.exchange(url, HttpMethod.GET, request, OidcUser.class);

    return UserTransformer.transformToDomain(response.getBody());
  }
}
