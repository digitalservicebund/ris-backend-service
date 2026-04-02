package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public enum Notation {
  OLD(Pattern.compile("\\d{2}-((\\d{2}-)*\\d{2}|\\d{3}N?)?"), "alt"),
  NEW(Pattern.compile("\\p{Lu}{2}((-\\d{2})*|-\\d{2}-\\p{Lu}{3})"), "neu");

  private final Pattern pattern;
  private final String germanName;

  Notation(Pattern pattern, String germanName) {
    this.pattern = pattern;
    this.germanName = germanName;
  }

  @Override
  public String toString() {
    return this.germanName;
  }
}
