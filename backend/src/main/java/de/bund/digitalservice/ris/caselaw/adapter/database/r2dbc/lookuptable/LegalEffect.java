package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import java.util.List;

public enum LegalEffect {
  YES("Ja"),
  NO("Nein"),
  NOT_SPECIFIED("Keine Angabe");

  private final String label;

  // as defined in RISDEV-628
  private static final List<String> autoYesCourtTypes =
      List.of("BGH", "BVerwG", "BFH", "BVerfG", "BAG", "BSG");

  LegalEffect(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public static String deriveFrom(DocumentUnit documentUnit, boolean courtHasNotChanged) {
    if (documentUnit == null || documentUnit.coreData() == null) {
      return null;
    }
    if (!courtHasNotChanged
        && documentUnit.coreData().court() != null
        && documentUnit.coreData().court().type() != null
        && autoYesCourtTypes.contains(documentUnit.coreData().court().type())) {
      return YES.getLabel();
    }
    return documentUnit.coreData().legalEffect();
  }
}
