package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.BareUserApiResponse;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UserDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class UserTransformer {

  private UserTransformer() {}

  public static User transformToDomain(
      BareUserApiResponse.BareUser bareUser, DocumentationOffice documentationOffice) {
    List<String> firstNames = getValues(bareUser.attributes(), "firstName");
    List<String> lastNames = getValues(bareUser.attributes(), "lastName");

    return User.builder()
        .externalId(bareUser.uuid())
        .name(
            getFullName(
                Stream.concat(firstNames.stream(), lastNames.stream()).toArray(String[]::new)))
        .firstName(String.join(" ", firstNames))
        .lastName(String.join(" ", lastNames))
        .email(bareUser.email())
        .initials(getInitials(firstNames, lastNames))
        .documentationOffice(documentationOffice)
        .build();
  }

  public static User transformToDomain(BareUserApiResponse.BareUser bareUser) {
    return transformToDomain(bareUser, null);
  }

  public static User transformToDomain(OidcUser oidcUser, DocumentationOffice documentationOffice) {
    return User.builder()
        .name(oidcUser.getAttribute("name"))
        .firstName(oidcUser.getGivenName())
        .lastName(oidcUser.getFamilyName())
        .externalId(getOidcUserId(oidcUser))
        .email(oidcUser.getEmail())
        .documentationOffice(documentationOffice)
        .roles(oidcUser.getClaimAsStringList("roles"))
        .initials(getInitials(oidcUser.getGivenName(), oidcUser.getFamilyName()))
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
        .name(getFullName(firstName, lastName))
        .firstName(firstName)
        .lastName(lastName)
        .initials(getInitials(firstName, lastName))
        .documentationOffice(
            DocumentationOfficeTransformer.transformToDomain(userDTO.getDocumentationOffice()))
        .build();
  }

  private static String getFullName(String... names) {
    return Arrays.stream(names)
        .filter(Objects::nonNull)
        .filter(s -> !s.isEmpty())
        .collect(
            Collectors.collectingAndThen(Collectors.joining(" "), s -> s.isEmpty() ? null : s));
  }

  private static String getInitials(String firstName, String lastName) {
    if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
      return null;
    }

    return ""
        + Character.toUpperCase(firstName.charAt(0))
        + Character.toUpperCase(lastName.charAt(0));
  }

  private static String getInitials(List<String> firstNames, List<String> lastNames) {
    if (firstNames.isEmpty() && lastNames.isEmpty()) {
      return null;
    }

    Optional<String> firstName =
        firstNames.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .findFirst();

    Optional<String> lastName =
        lastNames.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .findFirst();

    if (firstName.isEmpty() || lastName.isEmpty()) return null;

    return getInitials(firstName.get(), lastName.get());
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
        .isActive(true)
        .isDeleted(false)
        .build();
  }
}
