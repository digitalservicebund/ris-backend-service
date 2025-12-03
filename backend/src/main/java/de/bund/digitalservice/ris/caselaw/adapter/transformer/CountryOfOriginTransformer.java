package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CountryOfOriginDto;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FieldOfLawDTO;
import de.bund.digitalservice.ris.caselaw.domain.CountryOfOrigin;

/**
 * Utility class for transforming between {@link CountryOfOrigin} and {@link CountryOfOriginDto}.
 */
public class CountryOfOriginTransformer {

  private CountryOfOriginTransformer() {
    // Private constructor to prevent instantiation of this utility class.
  }

  /**
   * Transforms a domain object into it's database representation
   *
   * @param domainObject The domain object.
   * @param rank rank of the current domain object
   * @return A database representation of a domain object.
   */
  public static CountryOfOriginDto transformToDTO(CountryOfOrigin domainObject, long rank) {
    if (domainObject == null) {
      return null;
    }

    return CountryOfOriginDto.builder()
        .id(domainObject.newEntry() ? null : domainObject.id())
        .legacyValue(domainObject.legacyValue())
        .country(
            domainObject.country() != null
                ? FieldOfLawDTO.builder().id(domainObject.country().id()).build()
                : null)
        .fieldOfLaw(
            domainObject.fieldOfLaw() != null
                ? FieldOfLawDTO.builder().id(domainObject.fieldOfLaw().id()).build()
                : null)
        .rank(rank)
        .build();
  }

  /**
   * Transforms a DTO (Data Transfer Object) into a domain object.
   *
   * @param dto A database representation of the domain object.
   * @return The domain representation of the DTO.
   */
  public static CountryOfOrigin transformToDomain(CountryOfOriginDto dto) {
    if (dto == null) {
      return null;
    }

    return CountryOfOrigin.builder()
        .id(dto.getId())
        .legacyValue(dto.getLegacyValue())
        .country(FieldOfLawTransformer.transformToDomain(dto.getCountry(), false, false))
        .fieldOfLaw(FieldOfLawTransformer.transformToDomain(dto.getFieldOfLaw(), false, false))
        .rank(dto.getRank())
        .build();
  }
}
