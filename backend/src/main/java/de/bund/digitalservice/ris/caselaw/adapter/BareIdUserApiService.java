package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.UserTransformer;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserApiException;
import de.bund.digitalservice.ris.caselaw.domain.UserApiService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Service for retrieving user and group information from the BareId User API.
 *
 * @see <a href="https://api.bare.id/?urls.primaryName=user%2Fv1">Bare User API</a> for more
 *     details.
 */
@Service
@Slf4j
public class BareIdUserApiService implements UserApiService {

  private final BareIdUserApiTokenService bareIdUserApiTokenService;

  private final RestTemplate restTemplate;

  private final String bareidInstance;

  public BareIdUserApiService(
      BareIdUserApiTokenService bareIdUserApiTokenService,
      RestTemplate restTemplate,
      @Value("${bareid.instance}") String bareidInstance) {
    this.bareIdUserApiTokenService = bareIdUserApiTokenService;
    this.restTemplate = restTemplate;
    this.bareidInstance = bareidInstance;
  }

  @Override
  public User getUser(UUID userId) {
    try {

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(bareIdUserApiTokenService.getAccessToken().getTokenValue());
      HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

      String url = String.format("https://api.bare.id/user/v1/%s/users/%s", bareidInstance, userId);

      ResponseEntity<BareUserApiResponse.UserApiResponse> response =
          restTemplate.exchange(
              url, HttpMethod.GET, request, BareUserApiResponse.UserApiResponse.class);
      var responseBody = response.getBody();

      if (responseBody == null || responseBody.user() == null) {
        throw new UserApiException("User not found or could not be parsed");
      }

      return UserTransformer.transformToDomain(responseBody.user());

    } catch (Exception e) {
      return User.builder().id(userId).build();
    }
  }

  @Override
  public List<User> getUsers(String userGroupPathName) {
    if (StringUtils.isNullOrBlank(userGroupPathName)) {
      throw new UserApiException("User group path is empty or blank");
    }

    // Normalize and split: "/caselaw/BGH/Intern"
    List<String> userGroupsPathSegments =
        Arrays.stream(userGroupPathName.split("/")).filter(s -> !s.isBlank()).toList();

    if (userGroupsPathSegments.size() < 2) {
      throw new UserApiException(
          "User group path must contain at least two segments, e.g., \"caselaw/court\"");
    }

    // List all top level groups and get the current
    BareUserApiResponse.Group current =
        getGroupByName(getTopLevelGroups(), userGroupsPathSegments.getFirst());

    // Get the group court like the BGH
    BareUserApiResponse.Group court =
        getGroupByName(getGroupChildren(current.uuid()), userGroupsPathSegments.get(1));

    // Get all users under the  court
    return getUsersRecursively(court);
  }

  private List<User> getUsersRecursively(BareUserApiResponse.Group group) {
    List<User> users = getUsers(group.uuid());
    for (BareUserApiResponse.Group child : getGroupChildren(group.uuid())) {
      users.addAll(getUsers(child.uuid()));
      users.addAll(getUsersRecursively(child));
    }
    return users;
  }

  private BareUserApiResponse.Group getGroupByName(
      List<BareUserApiResponse.Group> groups, String groupName) {
    return groups.stream()
        .filter(item -> item.name().equals(groupName))
        .findFirst()
        .orElseThrow(() -> new UserApiException(groupName + " was not found in list"));
  }

  private List<BareUserApiResponse.Group> getGroupChildren(UUID groupId) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(bareIdUserApiTokenService.getAccessToken().getTokenValue());
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

    String url = String.format("https://api.bare.id/user/v1/%s/groups/%s", bareidInstance, groupId);

    ResponseEntity<BareUserApiResponse.GroupApiResponse> response =
        restTemplate.exchange(
            url, HttpMethod.GET, request, BareUserApiResponse.GroupApiResponse.class);
    if (response.getBody() == null) {
      throw new UserApiException("Children group could not be found");
    }

    return response.getBody().children().groups();
  }

  private List<BareUserApiResponse.Group> getTopLevelGroups() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(bareIdUserApiTokenService.getAccessToken().getTokenValue());
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

    String url = String.format("https://api.bare.id/user/v1/%s/groups", bareidInstance);

    ResponseEntity<BareUserApiResponse.GroupResponse> response =
        restTemplate.exchange(
            url, HttpMethod.GET, request, BareUserApiResponse.GroupResponse.class);
    if (response.getBody() == null) {
      throw new UserApiException("Top level groups could not be found");
    }

    return response.getBody().groups();
  }

  public List<User> getUsers(UUID userGroupId) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(bareIdUserApiTokenService.getAccessToken().getTokenValue());
      HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

      String subGroupUrl =
          String.format(
              "https://api.bare.id/user/v1/%s/groups/%s/users", bareidInstance, userGroupId);

      ResponseEntity<BareUserApiResponse.UsersApiResponse> subGroupResponse =
          restTemplate.exchange(
              subGroupUrl, HttpMethod.GET, request, BareUserApiResponse.UsersApiResponse.class);

      List<BareUserApiResponse.BareUser> apiUsers =
          Optional.ofNullable(subGroupResponse.getBody())
              .map(BareUserApiResponse.UsersApiResponse::users)
              .orElse(List.of());

      return apiUsers.stream().map(UserTransformer::transformToDomain).toList();

    } catch (Exception e) {
      log.error("Failed to fetch users for group {}: {}", userGroupId, e.getMessage(), e);
      return List.of();
    }
  }
}
