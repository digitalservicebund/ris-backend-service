package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CurrencyCodeService {
  private final CurrencyCodeRepository currencyCodeRepository;

  public CurrencyCodeService(CurrencyCodeRepository currencyCodeRepository) {
    this.currencyCodeRepository = currencyCodeRepository;
  }

  public List<CurrencyCode> getCurrencyCodes(String searchStr, Integer size) {
    if (searchStr != null && !searchStr.trim().isBlank()) {
      return currencyCodeRepository.findAllBySearchStr(searchStr.trim(), size);
    }

    return currencyCodeRepository.findAll(size);
  }
}
