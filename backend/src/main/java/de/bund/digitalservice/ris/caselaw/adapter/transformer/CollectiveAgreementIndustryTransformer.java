package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CollectiveAgreementIndustryDTO;
import de.bund.digitalservice.ris.caselaw.domain.CollectiveAgreementIndustry;

public class CollectiveAgreementIndustryTransformer {
  private CollectiveAgreementIndustryTransformer() {}

  public static CollectiveAgreementIndustry transformToDomain(
      CollectiveAgreementIndustryDTO collectiveAgreementIndustryDTO) {
    if (collectiveAgreementIndustryDTO == null) {
      return null;
    }

    return new CollectiveAgreementIndustry(
        collectiveAgreementIndustryDTO.getId(), collectiveAgreementIndustryDTO.getValue());
  }

  public static CollectiveAgreementIndustryDTO transformToDto(
      CollectiveAgreementIndustry collectiveAgreementIndustry) {
    if (collectiveAgreementIndustry == null) {
      return null;
    }

    return CollectiveAgreementIndustryDTO.builder().id(collectiveAgreementIndustry.id()).build();
  }
}
