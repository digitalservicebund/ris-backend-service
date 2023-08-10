package de.bund.digitalservice.ris.norms.domain.specification.section

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.specification.Specification
import de.bund.digitalservice.ris.norms.domain.specification.SpecificationResult
import de.bund.digitalservice.ris.norms.domain.specification.SpecificationViolation
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType

class HasAllMandatoryFields() : Specification<List<MetadataSection>> {
  private data class MandatoryField(
      val sectionName: MetadataSectionName,
      val metadatumTypes: List<MetadatumType>,
      val hasOrder: Boolean = false
  )

  private data class MissingMandatoryField(
      val sectionName: MetadataSectionName,
      val metadatumTypes: MutableList<MetadatumType>,
      val order: Int? = null
  )

  private val mandatoryFields: List<MandatoryField> =
      listOf(
          MandatoryField(MetadataSectionName.NORM, listOf(MetadatumType.OFFICIAL_LONG_TITLE)),
          //          MandatoryField(
          //              MetadataSectionName.DOCUMENT_TYPE,
          //              listOf(MetadatumType.TYPE_NAME, MetadatumType.NORM_CATEGORY)),
          //          MandatoryField(
          //              MetadataSectionName.NORM_PROVIDER,
          //              listOf(MetadatumType.ENTITY, MetadatumType.DECIDING_BODY),
          //              true)
      )

  override fun evaluate(instance: List<MetadataSection>): SpecificationResult {

    val missingFields = mutableListOf<MissingMandatoryField>()

    mandatoryFields.forEach { mandatoryField ->
      val sections = instance.filter { section -> section.name == mandatoryField.sectionName }
      if (sections.isEmpty()) {
        missingFields.add(
            MissingMandatoryField(
                mandatoryField.sectionName,
                mandatoryField.metadatumTypes.toMutableList(),
                if (mandatoryField.hasOrder) 1 else null))
      } else {
        sections.forEach { section ->
          mandatoryField.metadatumTypes.forEach { mandatoryMetadatumType ->
            val metadatum =
                section.metadata.firstOrNull { metadatum ->
                  metadatum.type == mandatoryMetadatumType
                }
            if (metadatum == null) {
              val existingPair = missingFields.find { it.sectionName == section.name }

              if (existingPair != null) {
                val existingList = existingPair.metadatumTypes
                existingList.add(mandatoryMetadatumType)
              } else {
                val newMissing =
                    MissingMandatoryField(
                        section.name,
                        mutableListOf(mandatoryMetadatumType),
                        if (mandatoryField.hasOrder) section.order else null)
                missingFields.add(newMissing)
              }
            }
          }
        }
      }
    }

    return if (missingFields.isEmpty()) {
      SpecificationResult.Satisfied
    } else
        SpecificationResult.Unsatisfied(
            missingFields.flatMap { missingField ->
              missingField.metadatumTypes.map { missingMetadatumType ->
                SpecificationViolation(
                    missingField.sectionName.name +
                        (if (missingField.order != null) "/${missingField.order}/" else "/") +
                        missingMetadatumType,
                    "MANDATORY_FIELD_MISSING",
                    "Norm does not contain the mandatory field $missingMetadatumType in section ${missingField.sectionName}" +
                        if (missingField.order != null) " order nr. ${missingField.order}" else "",
                )
              }
            })
  }
}
