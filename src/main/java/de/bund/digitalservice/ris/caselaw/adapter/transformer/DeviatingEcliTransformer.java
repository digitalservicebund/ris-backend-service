package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingEcliDTO;

public class DeviatingEcliTransformer {

  public static DeviatingEcliDTO enrichDTO(DeviatingEcliDTO deviatingEcliDTO, String ecli) {
    deviatingEcliDTO.setEcli(ecli);
    return deviatingEcliDTO;
  }
}
