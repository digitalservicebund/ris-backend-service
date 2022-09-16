package de.bund.digitalservice.ris.domain;

public record DocumentUnit(CoreData coreData, Texts texts) {
  public static final DocumentUnit EMPTY = new DocumentUnit(null, null);
}
