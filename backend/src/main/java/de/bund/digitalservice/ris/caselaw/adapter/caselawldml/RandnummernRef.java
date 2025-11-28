package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RandnummernRef extends Ref {
  public RandnummernRef(String domainTerm, String randnummer) {
    super(domainTerm, "#randnummer-" + randnummer, randnummer);
  }
}
