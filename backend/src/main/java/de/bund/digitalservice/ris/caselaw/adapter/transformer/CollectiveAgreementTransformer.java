package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CollectiveAgreementDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CollectiveAgreementIndustryDTO;
import de.bund.digitalservice.ris.caselaw.domain.CollectiveAgreement;

public class CollectiveAgreementTransformer {
  private CollectiveAgreementTransformer() {}

  public static CollectiveAgreement transformToDomain(
      CollectiveAgreementDTO collectiveAgreementDTO) {
    if (collectiveAgreementDTO == null) {
      return null;
    }

    return CollectiveAgreement.builder()
        .id(collectiveAgreementDTO.getId())
        .name(collectiveAgreementDTO.getName())
        .date(collectiveAgreementDTO.getDate())
        .norm(collectiveAgreementDTO.getNorm())
        .industry(
            CollectiveAgreementIndustryTransformer.transformToDomain(
                collectiveAgreementDTO.getIndustry()))
        .build();
  }

  public static CollectiveAgreementDTO transformToDTO(CollectiveAgreement collectiveAgreement) {
    if (collectiveAgreement == null) return null;

    var builder =
        CollectiveAgreementDTO.builder()
            .id(collectiveAgreement.newEntry() ? null : collectiveAgreement.id())
            .name(collectiveAgreement.name())
            .norm(collectiveAgreement.norm())
            .date(collectiveAgreement.date());

    if (collectiveAgreement.industry() != null) {
      builder.industry(
          CollectiveAgreementIndustryDTO.builder().id(collectiveAgreement.industry().id()).build());
    }

    return builder.build();
  }
}
