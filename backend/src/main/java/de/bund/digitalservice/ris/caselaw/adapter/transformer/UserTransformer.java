package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.BareUserApiResponse;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UserDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class UserTransformer {

  private UserTransformer() {}

  public static User transformToDomain(
      BareUserApiResponse.BareUser bareUser,
      DocumentationOffice documentationOffice,
      boolean internal) {
    List<String> firstNames = getValues(bareUser.attributes(), "firstName");
    List<String> lastNames = getValues(bareUser.attributes(), "lastName");

    return User.builder()
        .externalId(bareUser.uuid())
        .firstName(String.join(" ", firstNames))
        .lastName(String.join(" ", lastNames))
        .email(bareUser.email())
        .documentationOffice(documentationOffice)
        .internal(internal)
        .build();
  }

  public static User transformToDomain(BareUserApiResponse.BareUser bareUser) {
    return transformToDomain(bareUser, null, false);
  }

  public static User transformToDomain(OidcUser oidcUser, DocumentationOffice documentationOffice) {
    return User.builder()
        .firstName(oidcUser.getGivenName())
        .lastName(oidcUser.getFamilyName())
        .externalId(getOidcUserId(oidcUser))
        .email(oidcUser.getEmail())
        .documentationOffice(documentationOffice)
        .internal(oidcUser.getClaimAsStringList("roles").contains("Internal"))
        .build();
  }

  public static User transformToDomain(UserDTO userDTO) {
    if (userDTO == null) {
      return null;
    }
    String firstName = userDTO.getFirstName();
    String lastName = userDTO.getLastName();
    return User.builder()
        .id(userDTO.getId())
        .externalId(userDTO.getExternalId())
        .firstName(firstName)
        .lastName(lastName)
        .internal(userDTO.isInternal())
        .documentationOffice(
            DocumentationOfficeTransformer.transformToDomain(userDTO.getDocumentationOffice()))
        .build();
  }

  public static List<String> getValues(
      Map<String, BareUserApiResponse.AttributeValues> attributes, String key) {
    if (attributes == null || !attributes.containsKey(key)) {
      return Collections.emptyList();
    }
    return attributes.get(key).values();
  }

  public static UUID getOidcUserId(OidcUser oidcUser) {
    return Optional.ofNullable(oidcUser)
        .map(OidcUser::getSubject)
        .map(UUID::fromString)
        .orElse(null);
  }

  public static UserDTO transformToDTO(User user) {
    if (user == null) {
      return null;
    }
    return UserDTO.builder()
        .firstName(user.firstName())
        .lastName(user.lastName())
        .externalId(user.externalId())
        .id(user.id())
        .externalId(user.externalId())
        .documentationOffice(
            DocumentationOfficeTransformer.transformToDTO(user.documentationOffice()))
        .internal(user.internal())
        .isActive(true)
        .isDeleted(false)
        .build();
  }
}
