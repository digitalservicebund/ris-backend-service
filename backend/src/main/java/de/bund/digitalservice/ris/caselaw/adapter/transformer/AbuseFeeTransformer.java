package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AbuseFeeDTO;
import de.bund.digitalservice.ris.caselaw.domain.AbuseFee;

/**
 * Utility class for transforming AbuseFee objects between DTOs (Data Transfer Objects) and domain
 * objects.
 */
public class AbuseFeeTransformer {

  private AbuseFeeTransformer() {
    // Private constructor to prevent instantiation of this utility class.
  }

  /**
   * Transforms an AbuseFee domain object into it's database representation, a AbuseFeeDTO (Data
   * Transfer Object).
   *
   * @param abuseFee The abuse fee as a domain object.
   * @param index position of the current abuse fee
   * @return A database representation of a domain object.
   */
  public static AbuseFeeDTO transformToDTO(AbuseFee abuseFee, int index) {
    if (abuseFee == null) {
      return null;
    }

    return AbuseFeeDTO.builder()
        .id(abuseFee.newEntry() ? null : abuseFee.id())
        .amount(abuseFee.amount())
        .currencyCode(CurrencyCodeTransformer.transformToDTO(abuseFee.currencyCode()))
        .addressee(abuseFee.addressee())
        .rank(index + 1L)
        .build();
  }

  /**
   * Transforms an AbuseFeeDTO (Data Transfer Object) into an AbuseFee domain object.
   *
   * @param dto A database representation of the abuse fee domain object, to be transformed.
   * @return The domain representation of the abuse fee.
   */
  public static AbuseFee transformToDomain(AbuseFeeDTO dto) {
    if (dto == null) {
      return null;
    }

    return AbuseFee.builder()
        .id(dto.getId())
        .currencyCode(CurrencyCodeTransformer.transformToDomain(dto.getCurrencyCode()))
        .addressee(dto.getAddressee())
        .amount(dto.getAmount())
        .build();
  }
}
