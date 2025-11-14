package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CollectiveAgreementIndustryService {
  private final CollectiveAgreementIndustryRepository collectiveAgreementIndustryRepository;

  public CollectiveAgreementIndustryService(
      CollectiveAgreementIndustryRepository collectiveAgreementIndustryRepository) {
    this.collectiveAgreementIndustryRepository = collectiveAgreementIndustryRepository;
  }

  public List<CollectiveAgreementIndustry> getCollectiveAgreementIndustries(
      String searchStr, Integer size) {
    if (searchStr != null && !searchStr.trim().isBlank()) {
      return collectiveAgreementIndustryRepository.findAllBySearchStr(searchStr.trim(), size);
    }

    return collectiveAgreementIndustryRepository.findAll(size);
  }
}
