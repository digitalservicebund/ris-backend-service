package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.NormReferenceType;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AbstractNormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NonApplicationNormDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormAbbreviationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for transforming NormReference objects between DTOs (Data Transfer Objects) and
 * domain objects.
 */
public class NormReferenceTransformer {

  private NormReferenceTransformer() {
    // Private constructor to prevent instantiation of this utility class.
  }

  /**
   * Returns a list of NormReferences, where norm references with the same normAbbreviation are
   * grouped into a single NormReference.
   *
   * @param normReferenceDTOS A List of AbstractNormReferenceDTO objects, representing norm
   *     references and non-application norms.
   * @return A list of transformed and grouped NormReference objects.
   */
  static List<NormReference> transformToDomain(
      List<? extends AbstractNormReferenceDTO> normReferenceDTOS) {
    List<NormReference> normReferences = new ArrayList<>();

    normReferenceDTOS.forEach(
        normReferenceDTO -> {
          NormReference normReference =
              NormReferenceTransformer.transformAbstractNormReferenceToDomain(normReferenceDTO);

          if (normReferenceDTO.getNormAbbreviation() != null) {
            NormReference existingReference =
                normReferences.stream()
                    .filter(
                        existingNormReference ->
                            existingNormReference.normAbbreviation() != null
                                && existingNormReference
                                    .normAbbreviation()
                                    .id()
                                    .equals(normReferenceDTO.getNormAbbreviation().getId()))
                    .findFirst()
                    .orElse(null);

            if (existingReference != null) {
              existingReference
                  .singleNorms()
                  .add(SingleNormTransformer.transformToDomain(normReferenceDTO));
            } else {
              normReferences.add(normReference);
            }

          } else if (normReferenceDTO.getNormAbbreviationRawValue() != null) {
            NormReference existingReference =
                normReferences.stream()
                    .filter(
                        existingNormReference ->
                            existingNormReference.normAbbreviationRawValue() != null
                                && existingNormReference
                                    .normAbbreviationRawValue()
                                    .equals(normReferenceDTO.getNormAbbreviationRawValue()))
                    .findFirst()
                    .orElse(null);

            if (existingReference != null) {
              existingReference
                  .singleNorms()
                  .add(SingleNormTransformer.transformToDomain(normReferenceDTO));
            } else {
              normReferences.add(normReference);
            }
          }
        });

    // Handle cases where both abbreviation and raw value are null
    normReferences.addAll(
        normReferenceDTOS.stream()
            .filter(
                normReferenceDTO ->
                    normReferenceDTO.getNormAbbreviation() == null
                        && normReferenceDTO.getNormAbbreviationRawValue() == null)
            .map(NormReferenceTransformer::transformAbstractNormReferenceToDomain)
            .toList());

    return normReferences;
  }

  /**
   * Transforms a NormReferenceDTO (Data Transfer Object) into a {@link NormReference} domain
   * object, before all NormReferences with the same normAbbreviation are being grouped into one
   * NormReference object in the {@link DecisionTransformer}
   *
   * @param normDTO The NormReferenceDTO to be transformed.
   * @return The NormReference domain object representing the transformed NormReferenceDTO.
   */
  private static NormReference transformAbstractNormReferenceToDomain(
      AbstractNormReferenceDTO normDTO) {
    List<SingleNorm> list = new ArrayList<>();
    SingleNorm singleNorm = SingleNormTransformer.transformToDomain(normDTO);

    if (singleNorm != null) {
      list.add(singleNorm);
    }

    return NormReference.builder()
        .normAbbreviation(
            NormAbbreviationTransformer.transformToDomain(normDTO.getNormAbbreviation()))
        .normAbbreviationRawValue(normDTO.getNormAbbreviationRawValue())
        .singleNorms(list)
        .build();
  }

  public static List<AbstractNormReferenceDTO> transformToDTO(
      List<NormReference> normReferences, String type) {
    AtomicInteger i = new AtomicInteger(1);
    List<AbstractNormReferenceDTO> flattenNormReferenceDTOs = new ArrayList<>();
    normReferences.forEach(
        norm -> {
          List<AbstractNormReferenceDTO> normReferenceDTOs =
              transformAbstractToDTO(
                  norm,
                  NormReferenceType.NORM.equals(type)
                      ? NormReferenceDTO.builder()
                      : NonApplicationNormDTO.builder());
          normReferenceDTOs.forEach(
              normReferenceDTO -> normReferenceDTO.setRank(i.getAndIncrement()));
          flattenNormReferenceDTOs.addAll(normReferenceDTOs);
        });

    flattenNormReferenceDTOs.forEach(
        normReferenceDTO -> {
          if (normReferenceDTO.getLegalForce() != null)
            normReferenceDTO.getLegalForce().setNormReference(normReferenceDTO);
        });

    return flattenNormReferenceDTOs;
  }

  /**
   * Transforms a NormReference domain object into a list of NormReferenceDTOs (Data Transfer
   * Objects). When converting into a DTO object, each single norm in a normReference is converted
   * into its own {@link NormReferenceDTO}.
   *
   * @param normReference The NormReference object to be transformed.
   * @return A list of NormReferenceDTOs representing the transformed NormReference object.
   */
  private static List<AbstractNormReferenceDTO> transformAbstractToDTO(
      NormReference normReference,
      AbstractNormReferenceDTO.AbstractNormReferenceDTOBuilder<?, ?> builder) {
    if (normReference == null) {
      return Collections.emptyList();
    }

    if (normReference.normAbbreviation() == null
        && normReference.normAbbreviationRawValue() == null) {
      throw new DocumentationUnitTransformerException(
          "Norm reference has no norm abbreviation, but is required.");
    }

    NormAbbreviationDTO normAbbreviationDTO =
        normReference.normAbbreviation() != null
            ? NormAbbreviationDTO.builder().id(normReference.normAbbreviation().id()).build()
            : null;

    String normAbbreviationRawValue =
        normReference.normAbbreviationRawValue() != null
            ? normReference.normAbbreviationRawValue()
            : null;

    if (normReference.singleNorms() == null || normReference.singleNorms().isEmpty()) {
      return List.of(
          builder
              .normAbbreviation(normAbbreviationDTO)
              .normAbbreviationRawValue(normAbbreviationRawValue)
              .build());
    }

    List<AbstractNormReferenceDTO> normReferenceDTOS = new ArrayList<>();

    normReference
        .singleNorms()
        .forEach(
            singleNorm -> {
              builder
                  .id(singleNorm.id())
                  .normAbbreviation(normAbbreviationDTO)
                  .normAbbreviationRawValue(StringUtils.normalizeSpace(normAbbreviationRawValue))
                  .singleNorm(StringUtils.normalizeSpace(singleNorm.singleNorm()))
                  .dateOfVersion(singleNorm.dateOfVersion())
                  .dateOfRelevance(singleNorm.dateOfRelevance());
              if (singleNorm.legalForce() != null) {
                builder.legalForce(LegalForceTransformer.transformToDTO(singleNorm.legalForce()));
              }
              normReferenceDTOS.add(builder.build());
            });

    return normReferenceDTOS;
  }
}
