package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.BareUserApiResponse;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserTransformer {

  private UserTransformer() {}

  public static User transformToDomain(BareUserApiResponse.BareUser bareUser) {
    List<String> firstNames = getValues(bareUser.attributes(), "firstName");
    List<String> lastNames = getValues(bareUser.attributes(), "lastName");

    return User.builder()
        .id(bareUser.uuid())
        .name(getFullName(firstNames, lastNames))
        .email(bareUser.email())
        .initials(getInitials(firstNames, lastNames))
        .build();
  }

  private static String getFullName(List<String> firstNames, List<String> lastNames) {
    if (firstNames.isEmpty() && lastNames.isEmpty()) {
      return null;
    }

    return String.join(
        " ", java.util.stream.Stream.concat(firstNames.stream(), lastNames.stream()).toList());
  }

  private static String getInitials(List<String> firstNames, List<String> lastNames) {
    if (firstNames.isEmpty() && lastNames.isEmpty()) {
      return null;
    }

    String firstInitial =
        firstNames.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(s -> String.valueOf(Character.toUpperCase(s.charAt(0))))
            .findFirst()
            .orElse(null);

    String lastInitial =
        lastNames.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(s -> String.valueOf(Character.toUpperCase(s.charAt(0))))
            .findFirst()
            .orElse(null);

    if (firstInitial == null || lastInitial == null) return null;

    return firstInitial + lastInitial;
  }

  public static List<String> getValues(
      Map<String, BareUserApiResponse.AttributeValues> attributes, String key) {
    if (attributes == null || !attributes.containsKey(key)) {
      return Collections.emptyList();
    }
    return attributes.get(key).values();
  }
}
