package de.bund.digitalservice.ris.caselaw.domain;

import jakarta.mail.MessagingException;
import jakarta.mail.Store;

public interface MailStoreFactory {
  Store createStore() throws MessagingException;

  String getUsername();
}
