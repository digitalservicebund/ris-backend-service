package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingEcliDTO;
import de.bund.digitalservice.ris.caselaw.domain.DeviatingEcli;

public class DeviatingEcliTransformer {
  private DeviatingEcliTransformer() {}

  public static DeviatingEcliDTO transformToDTO(DeviatingEcli deviatingEcli, Long rank) {
    return DeviatingEcliDTO.builder().value(deviatingEcli.ecli()).rank(rank).build();
  }

  public static DeviatingEcli transformToDomain(DeviatingEcliDTO deviatingCourtDTO) {
    return DeviatingEcli.builder().ecli(deviatingCourtDTO.getValue()).build();
  }
}
