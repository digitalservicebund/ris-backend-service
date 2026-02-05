package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RandnummerRef extends Ref {
  public RandnummerRef(String domainTerm, String randnummer) {
    super(domainTerm, "#randnummer-" + randnummer, randnummer);
  }
}
