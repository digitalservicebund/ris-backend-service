package de.bund.digitalservice.ris.norms.domain.specification.section

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.specification.Specification
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.ADDITIONAL_INFO
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.AGE_OF_MAJORITY_INDICATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.ANNOUNCEMENT_GAZETTE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.ANNOUNCEMENT_MEDIUM
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.AREA_OF_PUBLICATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DATE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DEFINITION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DIVERGENT_DOCUMENT_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.EU_GOVERNMENT_GAZETTE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.EXPLANATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.KEYWORD
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.LEAD_JURISDICTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.LEAD_UNIT
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.OTHER_OFFICIAL_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PAGE_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_INSTITUTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_TYPE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RANGE_END
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RANGE_END_UNIT
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RANGE_START
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RANGE_START_UNIT
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.REFERENCE_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SERIES
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_BGB_3
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_FNA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_GESTA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_PREVIOUS_FNA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_ABBREVIATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_LONG_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_SHORT_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.VALIDITY_RULE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.YEAR
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName as Section

val hasValidMetadata =
    object : Specification<MetadataSection> {
        override fun isSatisfiedBy(instance: MetadataSection): Boolean = when (instance.name) {
            Section.NORM -> hasType(
                listOf(VALIDITY_RULE, AGE_OF_MAJORITY_INDICATION, DEFINITION, REFERENCE_NUMBER, UNOFFICIAL_REFERENCE, KEYWORD, DIVERGENT_DOCUMENT_NUMBER, RIS_ABBREVIATION_INTERNATIONAL_LAW, UNOFFICIAL_LONG_TITLE, UNOFFICIAL_SHORT_TITLE, UNOFFICIAL_ABBREVIATION),
                instance,
            )
            Section.SUBJECT_AREA -> hasType(listOf(SUBJECT_FNA, SUBJECT_PREVIOUS_FNA, SUBJECT_GESTA, SUBJECT_BGB_3), instance)
            Section.LEAD -> hasType(listOf(LEAD_JURISDICTION, LEAD_UNIT), instance)
            Section.PARTICIPATION -> hasType(listOf(PARTICIPATION_TYPE, PARTICIPATION_INSTITUTION), instance)
            Section.CITATION_DATE -> hasOneOfType(listOf(DATE, YEAR), instance)
            Section.AGE_IDENTIFICATION -> hasOnlyBlocksOf(
                listOf(listOf(RANGE_START, RANGE_START_UNIT), listOf(RANGE_END, RANGE_END_UNIT)),
                instance,
            )
            Section.PRINT_ANNOUNCEMENT -> hasType(listOf(ANNOUNCEMENT_GAZETTE, YEAR, NUMBER, PAGE_NUMBER, ADDITIONAL_INFO, EXPLANATION), instance)
            Section.DIGITAL_ANNOUNCEMENT -> hasType(listOf(ANNOUNCEMENT_MEDIUM, DATE, NUMBER, YEAR, PAGE_NUMBER, AREA_OF_PUBLICATION, NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA, ADDITIONAL_INFO, EXPLANATION), instance)
            Section.EU_GOVERNMENT_GAZETTE -> hasType(listOf(EU_GOVERNMENT_GAZETTE, YEAR, SERIES, NUMBER, PAGE_NUMBER, ADDITIONAL_INFO, EXPLANATION), instance)
            Section.OTHER_OFFICIAL_REFERENCE -> hasType(listOf(OTHER_OFFICIAL_REFERENCE), instance)
        }

        private fun hasType(types: List<MetadatumType>, instance: MetadataSection): Boolean =
            instance.metadata.all { types.contains(it.type) }
        private fun hasOneOfType(types: List<MetadatumType>, instance: MetadataSection): Boolean =
            instance.metadata.count() == 1 && hasType(types, instance)

        private fun hasOnlyBlocksOf(types: List<List<MetadatumType>>, instance: MetadataSection): Boolean {
            if (!hasType(types.flatten(), instance)) {
                return false
            }

            var result = true
            types.forEach { typeList ->
                if (instance.metadata.any { typeList.contains(it.type) }) {
                    val all = typeList.all { type -> instance.metadata.any { type == it.type } }
                    result = result and all
                }
            }
            return result
        }
    }
