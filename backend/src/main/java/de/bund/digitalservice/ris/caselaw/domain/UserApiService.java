package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;

public interface UserApiService {

  User getUser(UUID userId);

  List<User> getUsers(String userGroupPathName);
}
