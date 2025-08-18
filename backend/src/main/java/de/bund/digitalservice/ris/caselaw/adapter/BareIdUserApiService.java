package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.UserTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserApiException;
import de.bund.digitalservice.ris.caselaw.domain.UserApiService;
import de.bund.digitalservice.ris.caselaw.domain.UserGroup;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class BareIdUserApiService implements UserApiService {
  private static final Logger LOGGER = LoggerFactory.getLogger(BareIdUserApiService.class);
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

      // --- START get docoffice for user---
      HttpHeaders groupsHeaders = new HttpHeaders();
      groupsHeaders.setBearerAuth(bareIdUserApiTokenService.getAccessToken().getTokenValue());
      HttpEntity<MultiValueMap<String, String>> groupsRequest = new HttpEntity<>(groupsHeaders);

      String groupURL =
          String.format("https://api.bare.id/user/v1/%s/users/%s/groups", bareidInstance, userId);
      ResponseEntity<BareUserApiResponse.GroupApiResponse> groupsResponse =
          restTemplate.exchange(
              groupURL, HttpMethod.GET, groupsRequest, BareUserApiResponse.GroupApiResponse.class);
      if (groupsResponse.getBody() == null) {
        throw new UserApiException("User group could not be found");
      }

      DocumentationOffice docOffice =
          getDocumentationOfficeFromGroups(groupsResponse.getBody().groups()).orElse(null);
      // --- END get docoffice for user ---

      return UserTransformer.transformToDomain(responseBody.user(), docOffice);

    } catch (Exception e) {
      return User.builder().id(userId).build();
    }
  }

  @Override
  public List<User> getUsers(String userGroupPathName) {

    if (StringUtils.isNullOrBlank(userGroupPathName)) {
      throw new UserApiException("User group path is empty or blank");
    }

    String rootLevelUserGroup = userGroupPathName.substring(0, userGroupPathName.indexOf('/', 1));

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(bareIdUserApiTokenService.getAccessToken().getTokenValue());
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

    String url = String.format("https://api.bare.id/user/v1/%s/groups", bareidInstance);
    ResponseEntity<BareUserApiResponse.GroupApiResponse> response =
        restTemplate.exchange(
            url, HttpMethod.GET, request, BareUserApiResponse.GroupApiResponse.class);
    if (response.getBody() == null) {
      throw new UserApiException("User group could not be found");
    }

    var rootUserGroup =
        response.getBody().groups().stream()
            .filter(item -> item.path().equals(rootLevelUserGroup))
            .findFirst()
            .orElseThrow(() -> new UserApiException("Root user group was not found for path"));

    return getUsers(rootUserGroup.uuid());
  }

  public List<User> getUsers(UUID userGroupId) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(bareIdUserApiTokenService.getAccessToken().getTokenValue());
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

    String subGroupUrl =
        String.format(
            "https://api.bare.id/user/v1/%s/groups/%s/users", bareidInstance, userGroupId);
    ResponseEntity<BareUserApiResponse.UsersApiResponse> subGroupResponse =
        restTemplate.exchange(
            subGroupUrl, HttpMethod.GET, request, BareUserApiResponse.UsersApiResponse.class);

    if (subGroupResponse.getBody() == null) {
      throw new UserApiException("Could not fetch users");
    }

    return subGroupResponse.getBody().users().stream()
        .map(UserTransformer::transformToDomain)
        .toList();
  }

  private Optional<DocumentationOffice> getDocumentationOfficeFromGroups(
      List<BareUserApiResponse.Group> userGroups) {
    List<String> userGroupPaths = userGroups.stream().map(BareUserApiResponse.Group::path).toList();

    var uniqueDocOffices =
        this.userGroupService.getAllUserGroups().stream()
            .filter(group -> userGroupPaths.contains(group.userGroupPathName()))
            .map(UserGroup::docOffice)
            .distinct()
            .toList();

    if (uniqueDocOffices.isEmpty()) {
      LOGGER.warn(
          "No doc office user group associated with given Keycloak user groups: {}", userGroups);
      return Optional.empty();
    }

    if (uniqueDocOffices.size() > 1) {
      LOGGER.warn(
          "More then one doc office associated with given Keycloak user groups: {}", userGroups);
      throw new IllegalStateException("Multiple doc offices found.");
    }
    return uniqueDocOffices.stream().findFirst();
  }
}
