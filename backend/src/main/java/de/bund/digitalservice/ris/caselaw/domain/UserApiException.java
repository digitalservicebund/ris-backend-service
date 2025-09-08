package de.bund.digitalservice.ris.caselaw.domain;

import org.springframework.web.client.RestClientException;

public class UserApiException extends Exception {
  public UserApiException(String message) {
    super(message);
  }

  public UserApiException(String message, RestClientException ex) {
    super(message, ex);
  }
}
