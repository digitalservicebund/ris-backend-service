package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.BareUserApiResponse;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class UserTransformer {

  private UserTransformer() {}

  public static User transformToDomain(
      BareUserApiResponse.BareUser bareUser, DocumentationOffice documentationOffice) {
    List<String> firstNames = getValues(bareUser.attributes(), "firstName");
    List<String> lastNames = getValues(bareUser.attributes(), "lastName");

    return User.builder()
        .id(bareUser.uuid())
        .name(getFullName(firstNames, lastNames))
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
        .id(getUserId(oidcUser))
        .email(oidcUser.getEmail())
        .documentationOffice(documentationOffice)
        .roles(oidcUser.getClaimAsStringList("roles"))
        .initials(getInitials(oidcUser.getGivenName(), oidcUser.getFamilyName()))
        .build();
  }

  private static String getFullName(List<String> firstNames, List<String> lastNames) {
    if (firstNames.isEmpty() && lastNames.isEmpty()) {
      return null;
    }

    return String.join(
        " ", java.util.stream.Stream.concat(firstNames.stream(), lastNames.stream()).toList());
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
        firstNames.stream()
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

  public static UUID getUserId(OidcUser oidcUser) {
    return Optional.ofNullable(oidcUser.getSubject()).map(UUID::fromString).orElse(null);
  }
}
