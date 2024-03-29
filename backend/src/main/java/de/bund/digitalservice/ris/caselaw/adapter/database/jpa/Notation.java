package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public enum Notation {
  OLD(Pattern.compile("\\d{2}-((\\d{2}-)*\\d{2}|\\d{3}N?)?")),
  NEW(Pattern.compile("\\p{Lu}{2}((-\\d{2})*|-\\d{2}-\\p{Lu}{3})"));

  private final Pattern pattern;

  Notation(Pattern pattern) {
    this.pattern = pattern;
  }

  @Override
  public String toString() {
    return name();
  }
}
