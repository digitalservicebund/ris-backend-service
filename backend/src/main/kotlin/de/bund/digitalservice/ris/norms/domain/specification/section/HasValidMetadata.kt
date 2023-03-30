package de.bund.digitalservice.ris.norms.domain.specification.section

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.specification.Specification
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.AGE_OF_MAJORITY_INDICATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DEFINITION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DIVERGENT_DOCUMENT_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.KEYWORD
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.LEAD_JURISDICTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.LEAD_UNIT
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_INSTITUTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_TYPE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.REFERENCE_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_BGB_3
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_FNA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_GESTA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_PREVIOUS_FNA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_ABBREVIATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_LONG_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_SHORT_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.VALIDITY_RULE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName as Section

val hasValidMetadata =
    object : Specification<MetadataSection> {
        override fun isSatisfiedBy(instance: MetadataSection): Boolean = when (instance.name) {
            Section.GENERAL_INFORMATION -> hasType(listOf(KEYWORD, DIVERGENT_DOCUMENT_NUMBER, RIS_ABBREVIATION_INTERNATIONAL_LAW), instance)
            Section.HEADINGS_AND_ABBREVIATIONS -> hasType(listOf(UNOFFICIAL_LONG_TITLE, UNOFFICIAL_SHORT_TITLE, UNOFFICIAL_ABBREVIATION), instance)
            Section.UNOFFICIAL_REFERENCE -> hasType(listOf(UNOFFICIAL_REFERENCE), instance)
            Section.REFERENCE_NUMBER -> hasType(listOf(REFERENCE_NUMBER), instance)
            Section.DEFINITION -> hasType(listOf(DEFINITION), instance)
            Section.AGE_OF_MAJORITY_INDICATION -> hasType(listOf(AGE_OF_MAJORITY_INDICATION), instance)
            Section.VALIDITY_RULE -> hasType(listOf(VALIDITY_RULE), instance)
            Section.SUBJECT_AREA -> hasType(listOf(SUBJECT_FNA, SUBJECT_PREVIOUS_FNA, SUBJECT_GESTA, SUBJECT_BGB_3), instance)
            Section.LEAD -> hasType(listOf(LEAD_JURISDICTION, LEAD_UNIT), instance)
            Section.PARTICIPATING_INSTITUTIONS -> hasType(listOf(PARTICIPATION_TYPE, PARTICIPATION_INSTITUTION), instance)
        }

        private fun hasType(types: List<MetadatumType>, instance: MetadataSection): Boolean = instance.metadata.all { types.contains(it.type) }
    }
