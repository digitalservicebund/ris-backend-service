package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.BareUserApiResponse;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UserTransformer {

  private UserTransformer() {}

  public static User transformToDomain(BareUserApiResponse.BareUser bareUser) {

    return User.builder()
        .id(bareUser.uuid())
        .name(String.join(" ", getFullName(bareUser.attributes())))
        .email(bareUser.email())
        .build();
  }

  public static List<String> getFullName(
      Map<String, BareUserApiResponse.AttributeValues> attributes) {
    return java.util.stream.Stream.concat(
            getValues(attributes, "firstName").stream(), getValues(attributes, "lastName").stream())
        .toList();
  }

  public static List<String> getValues(
      Map<String, BareUserApiResponse.AttributeValues> attributes, String key) {
    if (attributes == null || !attributes.containsKey(key)) {
      return Collections.emptyList();
    }
    List<String> values = attributes.get(key).values();
    return values != null ? values : Collections.emptyList();
  }
}
