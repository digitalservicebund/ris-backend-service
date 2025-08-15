package de.bund.digitalservice.ris.caselaw.adapter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A class to map the v1GetUserResponse response object
 *
 * @see <a href="https://api.bare.id/?urls.primaryName=user%2Fv1">Bare User API</a> for more
 *     details.
 */
public class BareUserApiResponse {

  public record UserApiResponse(BareUser user) {}

  public record UsersApiResponse(List<BareUser> users) {}

  public record BareUser(
      UUID uuid,
      boolean enabled,
      boolean emailVerified,
      String username,
      String email,
      Map<String, AttributeValues> attributes) {}

  public record GroupApiResponse(List<Group> groups) {}

  public record Group(UUID uuid, String name, String path) {}

  public record AttributeValues(List<String> values) {}
}
