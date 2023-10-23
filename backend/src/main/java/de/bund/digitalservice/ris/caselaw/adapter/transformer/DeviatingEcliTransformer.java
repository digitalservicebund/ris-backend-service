package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingEcliDTO;
import de.bund.digitalservice.ris.caselaw.domain.DeviatingEcli;

public class DeviatingEcliTransformer {
  private DeviatingEcliTransformer() {}

  public static DeviatingEcliDTO enrichDTO(DeviatingEcliDTO deviatingEcliDTO, String ecli) {
    deviatingEcliDTO.setEcli(ecli);
    return deviatingEcliDTO;
  }

  public static de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingEcliDTO
      transformToDTO(DeviatingEcli deviatingEcli) {
    return de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingEcliDTO.builder()
        .id(deviatingEcli.id())
        .value(deviatingEcli.ecli())
        .build();
  }

  public static DeviatingEcli transformToDomain(
      de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingEcliDTO deviatingCourtDTO) {
    return DeviatingEcli.builder()
        .id(deviatingCourtDTO.getId())
        .ecli(deviatingCourtDTO.getValue())
        .build();
  }
}
