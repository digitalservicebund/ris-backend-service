package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ObjectValueDTO;
import de.bund.digitalservice.ris.caselaw.domain.ObjectValue;

/**
 * Utility class for transforming ObjectValue objects between DTOs (Data Transfer Objects) and
 * domain objects.
 */
public class ObjectValueTransformer {

  private ObjectValueTransformer() {
    // Private constructor to prevent instantiation of this utility class.
  }

  /**
   * Transforms an ObjectValue domain object into it's database representation, a ObjectValueDTO
   * (Data Transfer Object).
   *
   * @param objectValue The object value as a domain object.
   * @param index position of the current object value
   * @return A database representation of a domain object.
   */
  public static ObjectValueDTO transformToDTO(ObjectValue objectValue, int index) {
    if (objectValue == null) {
      return null;
    }

    return ObjectValueDTO.builder()
        .id(objectValue.id())
        .amount(objectValue.amount())
        .currencyCode(CurrencyCodeTransformer.transformToDTO(objectValue.currencyCode()))
        .proceedingType(objectValue.proceedingType())
        .rank(index + 1L)
        .build();
  }

  /**
   * Transforms a ObjectValueDTO (Data Transfer Object) into a ObjectValue domain object.
   *
   * @param dto A database representation of the object value domain object, to be transformed.
   * @return The domain representation of the object value.
   */
  public static ObjectValue transformToDomain(ObjectValueDTO dto) {
    if (dto == null) {
      return null;
    }

    return ObjectValue.builder()
        .id(dto.getId())
        .currencyCode(CurrencyCodeTransformer.transformToDomain(dto.getCurrencyCode()))
        .proceedingType(dto.getProceedingType())
        .amount(dto.getAmount())
        .build();
  }
}
