package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;

public interface UserApiService {

  User getUser(UUID userId) throws UserApiException;

  List<User> getUsers(String userGroupPathName) throws UserApiException;

  List<User> getUsers(UUID userGroupId) throws UserApiException;
}
