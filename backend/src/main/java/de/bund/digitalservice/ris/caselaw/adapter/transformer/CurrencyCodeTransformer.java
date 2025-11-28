package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CurrencyCodeDTO;
import de.bund.digitalservice.ris.caselaw.domain.CurrencyCode;

public class CurrencyCodeTransformer {

  private CurrencyCodeTransformer() {}

  public static CurrencyCode transformToDomain(CurrencyCodeDTO currencyCodeDTO) {
    if (currencyCodeDTO == null) {
      return null;
    }
    return CurrencyCode.builder()
        .id(currencyCodeDTO.getId())
        .label(currencyCodeDTO.getValue())
        .isoCode(currencyCodeDTO.getIsoCode())
        .build();
  }

  public static CurrencyCodeDTO transformToDTO(CurrencyCode currencyCode) {
    if (currencyCode == null) {
      return null;
    }
    return CurrencyCodeDTO.builder()
        .id(currencyCode.id())
        .value(currencyCode.label())
        .isoCode(currencyCode.isoCode())
        .build();
  }
}
