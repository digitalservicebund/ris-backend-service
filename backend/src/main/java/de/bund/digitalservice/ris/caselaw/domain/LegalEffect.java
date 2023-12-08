package de.bund.digitalservice.ris.caselaw.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum LegalEffect {
  YES("Ja"),
  NO("Nein"),
  NOT_SPECIFIED("Keine Angabe");

  private final String label;

  // as defined in RISDEV-628
  private static final List<String> autoYesCourtTypes =
      List.of("BGH", "BVerwG", "BFH", "BVerfG", "BAG", "BSG");

  private static final Map<String, LegalEffect> map = new HashMap<>(values().length, 1);

  static {
    for (LegalEffect c : values()) map.put(c.label, c);
  }

  LegalEffect(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public static LegalEffect of(String name) {
    LegalEffect result = map.get(name);

    if (result == null) {
      throw new IllegalArgumentException("Invalid category name: " + name);
    }

    return result;
  }

  @Override
  public String toString() {
    return label;
  }

  public static LegalEffect deriveFrom(DocumentUnit documentUnit, boolean courtHasChanged) {
    if (documentUnit == null
        || documentUnit.coreData() == null
        || documentUnit.coreData().legalEffect() == null) {
      return null;
    }

    if (courtHasChanged
        && documentUnit.coreData().court() != null
        && documentUnit.coreData().court().type() != null
        && autoYesCourtTypes.contains(documentUnit.coreData().court().type())) {
      return YES;
    }

    return of(documentUnit.coreData().legalEffect());
  }
}
