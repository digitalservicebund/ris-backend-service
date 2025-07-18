package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LanguageCodeService {
  private final LanguageCodeRepository languageCodeRepository;

  public LanguageCodeService(LanguageCodeRepository languageCodeRepository) {
    this.languageCodeRepository = languageCodeRepository;
  }

  public List<LanguageCode> getLanguageCodes(Optional<String> searchStr) {

    return languageCodeRepository.findAllBySearchStr(searchStr);
  }
}
