package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.BareUserApiResponse.UserApiResponse;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.UserTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserApiException;
import de.bund.digitalservice.ris.caselaw.domain.UserApiService;
import de.bund.digitalservice.ris.caselaw.domain.UserGroup;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.springframework.web.client.RestClientException;
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
  private final UserGroupService userGroupService;

  private final RestTemplate restTemplate;

  private final String bareidInstance;

  public BareIdUserApiService(
      BareIdUserApiTokenService bareIdUserApiTokenService,
      UserGroupService userGroupService,
      RestTemplate restTemplate,
      @Value("${bareid.instance}") String bareidInstance) {
    this.bareIdUserApiTokenService = bareIdUserApiTokenService;
    this.userGroupService = userGroupService;
    this.restTemplate = restTemplate;
    this.bareidInstance = bareidInstance;
  }

  @Override
  public User getUser(UUID externalId) throws UserApiException {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(bareIdUserApiTokenService.getAccessToken().getTokenValue());
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

    String url =
        String.format("https://api.bare.id/user/v1/%s/users/%s", bareidInstance, externalId);

    ResponseEntity<BareUserApiResponse.UserApiResponse> response;
    try {
      response =
          restTemplate.exchange(
              url, HttpMethod.GET, request, BareUserApiResponse.UserApiResponse.class);
    } catch (RestClientException ex) {
      throw new UserApiException("Error by fetching user", ex);
    }

    UserApiResponse responseBody = response.getBody();
    if (responseBody == null || responseBody.user() == null) {
      throw new UserApiException("User not found or could not be parsed");
    }

    Optional<UserGroup> group = getDocumentationOfficeForUser(externalId);
    DocumentationOffice docOffice = group.map(UserGroup::docOffice).orElse(null);
    boolean internal = group.map(UserGroup::isInternal).orElse(false);

    return UserTransformer.transformToDomain(responseBody.user(), docOffice, internal);
  }

  private Optional<UserGroup> getDocumentationOfficeForUser(UUID userId) throws UserApiException {
    HttpHeaders groupsHeaders = new HttpHeaders();
    groupsHeaders.setBearerAuth(bareIdUserApiTokenService.getAccessToken().getTokenValue());
    HttpEntity<MultiValueMap<String, String>> groupsRequest = new HttpEntity<>(groupsHeaders);

    String groupURL =
        String.format("https://api.bare.id/user/v1/%s/users/%s/groups", bareidInstance, userId);

    ResponseEntity<BareUserApiResponse.GroupResponse> groupsResponse;
    try {
      groupsResponse =
          restTemplate.exchange(
              groupURL, HttpMethod.GET, groupsRequest, BareUserApiResponse.GroupResponse.class);
    } catch (RestClientException ex) {
      throw new UserApiException("Error by getting groups for user", ex);
    }

    if (groupsResponse.getBody() == null) {
      throw new UserApiException("User group could not be found");
    }

    return getInternalUserGroupForExternalUserGroups(groupsResponse.getBody().groups());
  }

  @Override
  public List<User> getUsers(String userGroupPathName) throws UserApiException {
    if (StringUtils.isNullOrBlank(userGroupPathName)) {
      log.error("User group path is empty or blank");
      return Collections.emptyList();
    }

    // Normalize and split: "/caselaw/BGH/Intern"
    List<String> userGroupsPathSegments =
        Arrays.stream(userGroupPathName.split("/")).filter(s -> !s.isBlank()).toList();

    if (userGroupsPathSegments.size() < 2) {
      log.error("User group path must contain at least two segments, e.g., \"caselaw/court\"");
      return Collections.emptyList();
    }

    // List all top level groups and get the current
    BareUserApiResponse.Group current =
        getGroupByName(getTopLevelGroups(), userGroupsPathSegments.getFirst());

    // Get the group court like the BGH
    BareUserApiResponse.Group court =
        getGroupByName(getGroupChildren(current.uuid()), userGroupsPathSegments.get(1));

    // Get all users under the  court
    return getUsersRecursively(court, userGroupPathName);
  }

  private List<User> getUsersRecursively(BareUserApiResponse.Group group, String groupName)
      throws UserApiException {
    List<User> users = new ArrayList<>();
    if (group == null) {
      return users;
    }
    if (group.path().equals(groupName)) {
      users.addAll(getUsers(group.uuid()));
    } else {
      for (BareUserApiResponse.Group child : getGroupChildren(group.uuid())) {
        users.addAll(getUsersRecursively(child, groupName));
      }
    }
    return users;
  }

  private BareUserApiResponse.Group getGroupByName(
      List<BareUserApiResponse.Group> groups, String groupName) {
    return groups.stream().filter(item -> item.name().equals(groupName)).findFirst().orElse(null);
  }

  private List<BareUserApiResponse.Group> getGroupChildren(UUID groupId) throws UserApiException {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(bareIdUserApiTokenService.getAccessToken().getTokenValue());
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

    String url = String.format("https://api.bare.id/user/v1/%s/groups/%s", bareidInstance, groupId);

    ResponseEntity<BareUserApiResponse.GroupApiResponse> response;
    try {
      response =
          restTemplate.exchange(
              url, HttpMethod.GET, request, BareUserApiResponse.GroupApiResponse.class);
    } catch (RestClientException ex) {
      throw new UserApiException("Error while fetching users", ex);
    }

    if (response.getBody() == null || response.getBody().children() == null) {
      throw new UserApiException("Children for group could not be found");
    }

    return response.getBody().children().groups();
  }

  private List<BareUserApiResponse.Group> getTopLevelGroups() throws UserApiException {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(bareIdUserApiTokenService.getAccessToken().getTokenValue());
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

    String url = String.format("https://api.bare.id/user/v1/%s/groups", bareidInstance);

    ResponseEntity<BareUserApiResponse.GroupResponse> response;
    try {
      response =
          restTemplate.exchange(
              url, HttpMethod.GET, request, BareUserApiResponse.GroupResponse.class);
    } catch (RestClientException ex) {
      throw new UserApiException("Error while fetching top level groups", ex);
    }

    if (response.getBody() == null || response.getBody().groups() == null) {
      throw new UserApiException("Top level groups could not be found");
    }

    return response.getBody().groups();
  }

  public List<User> getUsers(UUID userGroupId) throws UserApiException {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(bareIdUserApiTokenService.getAccessToken().getTokenValue());
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

    String subGroupUrl =
        String.format(
            "https://api.bare.id/user/v1/%s/groups/%s/users", bareidInstance, userGroupId);

    ResponseEntity<BareUserApiResponse.UsersApiResponse> subGroupResponse;
    try {
      subGroupResponse =
          restTemplate.exchange(
              subGroupUrl, HttpMethod.GET, request, BareUserApiResponse.UsersApiResponse.class);
    } catch (RestClientException ex) {
      throw new UserApiException("Error while fetching users", ex);
    }

    if (subGroupResponse.getBody() == null || subGroupResponse.getBody().users() == null) {
      throw new UserApiException("Could not fetch users");
    }

    return subGroupResponse.getBody().users().stream()
        .map(UserTransformer::transformToDomain)
        .toList();
  }

  private Optional<UserGroup> getInternalUserGroupForExternalUserGroups(
      List<BareUserApiResponse.Group> userGroups) {
    if (userGroups == null || userGroups.isEmpty()) {
      return Optional.empty();
    }

    List<String> userGroupPaths = userGroups.stream().map(BareUserApiResponse.Group::path).toList();

    return this.userGroupService.getUserGroupFromGroupPathNames(userGroupPaths);
  }
}
