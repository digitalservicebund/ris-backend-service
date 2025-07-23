package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.User;
import java.util.UUID;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class BareIdUserApiService {

  private final BareIdUserApiTokenService bareIdUserApiTokenService;

  public BareIdUserApiService(BareIdUserApiTokenService bareIdUserApiTokenService) {
    this.bareIdUserApiTokenService = bareIdUserApiTokenService;
  }

  public User getUser(UUID userId) {
    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(bareIdUserApiTokenService.getAccessToken().getTokenValue());
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

    // TODO: Inject through application yml
    var instanceId = "random_id_to_be_added_later";
    String userDetailUrl = "https://api.bare.id/user/v1/" + instanceId + "/users/" + userId;

    // TODO: Map response object and build user
    var response = restTemplate.postForEntity(userDetailUrl, request, String.class);
    return User.builder().build();
  }
}
