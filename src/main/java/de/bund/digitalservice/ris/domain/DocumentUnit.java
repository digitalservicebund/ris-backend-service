package de.bund.digitalservice.ris.domain;

public record DocumentUnit(CoreData coreData, Categories categories) {
  public static final DocumentUnit EMPTY = new DocumentUnit(null, null);
}
