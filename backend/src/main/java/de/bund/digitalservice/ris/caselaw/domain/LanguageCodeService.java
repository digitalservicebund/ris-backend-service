package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LanguageCodeService {
  private final LanguageCodeRepository languageCodeRepository;

  public LanguageCodeService(LanguageCodeRepository languageCodeRepository) {
    this.languageCodeRepository = languageCodeRepository;
  }

  public List<LanguageCode> getLanguageCodes(String searchStr, Integer size) {
    if (searchStr != null && !searchStr.trim().isBlank()) {
      return languageCodeRepository.findAllBySearchStr(searchStr.trim(), size);
    }

    return languageCodeRepository.findAll(size);
  }
}
