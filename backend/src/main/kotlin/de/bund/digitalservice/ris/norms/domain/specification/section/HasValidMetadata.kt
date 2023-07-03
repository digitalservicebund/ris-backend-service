package de.bund.digitalservice.ris.norms.domain.specification.section

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.specification.Specification
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.ADDITIONAL_INFO
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.AGE_OF_MAJORITY_INDICATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.ANNOUNCEMENT_GAZETTE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.ANNOUNCEMENT_MEDIUM
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.APPENDIX
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.AREA_OF_PUBLICATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.ARTICLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.CELEX_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.COMPLETE_CITATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DATE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DECIDING_BODY
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DEFINITION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DESCRIPTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DIVERGENT_DOCUMENT_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DOCUMENT_CATEGORY
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DOCUMENT_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.EDITION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.ENTITY
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.ENTRY_INTO_FORCE_DATE_NOTE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.EU_GOVERNMENT_GAZETTE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.EXPLANATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.EXTERNAL_DATA_NOTE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.FOOTNOTE_CHANGE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.FOOTNOTE_COMMENT
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.FOOTNOTE_DECISION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.FOOTNOTE_EU_LAW
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.FOOTNOTE_OTHER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.FOOTNOTE_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.FOOTNOTE_STATE_LAW
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.KEYWORD
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.LEAD_JURISDICTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.LEAD_UNIT
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.LINK
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.NORM_CATEGORY
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.NOTE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.OFFICIAL_ABBREVIATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.OFFICIAL_LONG_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.OFFICIAL_SHORT_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.OTHER_OFFICIAL_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.OTHER_TYPE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PAGE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_INSTITUTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_TYPE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PROOF_INDICATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PROOF_TYPE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RANGE_END
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RANGE_START
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.REFERENCE_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RELATED_DATA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RESOLUTION_MAJORITY
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RIS_ABBREVIATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SERIES
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_BGB_3
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_FNA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_GESTA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_PREVIOUS_FNA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.TEMPLATE_NAME
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.TEXT
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.TIME
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.TYPE_NAME
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNDEFINED_DATE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_ABBREVIATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_LONG_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_SHORT_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.VALIDITY_RULE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.WORK_NOTE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.YEAR
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName as Section

val hasValidMetadata =
    object : Specification<MetadataSection> {
        override fun isSatisfiedBy(instance: MetadataSection): Boolean = when (instance.name) {
            Section.NORM -> hasType(
                listOf(
                    VALIDITY_RULE, AGE_OF_MAJORITY_INDICATION, DEFINITION, REFERENCE_NUMBER,
                    UNOFFICIAL_REFERENCE, KEYWORD, DIVERGENT_DOCUMENT_NUMBER, RIS_ABBREVIATION_INTERNATIONAL_LAW,
                    UNOFFICIAL_LONG_TITLE, UNOFFICIAL_SHORT_TITLE, UNOFFICIAL_ABBREVIATION, RIS_ABBREVIATION,
                    DOCUMENT_NUMBER, DOCUMENT_CATEGORY, OFFICIAL_SHORT_TITLE, OFFICIAL_ABBREVIATION,
                    COMPLETE_CITATION, CELEX_NUMBER, TEXT, OFFICIAL_LONG_TITLE,
                ),
                instance,
            )
            Section.SUBJECT_AREA -> hasType(listOf(SUBJECT_FNA, SUBJECT_PREVIOUS_FNA, SUBJECT_GESTA, SUBJECT_BGB_3), instance)
            Section.LEAD -> hasType(listOf(LEAD_JURISDICTION, LEAD_UNIT), instance)
            Section.PARTICIPATION -> hasType(listOf(PARTICIPATION_TYPE, PARTICIPATION_INSTITUTION), instance)
            Section.CITATION_DATE -> hasOneOfType(listOf(DATE, YEAR), instance)
            Section.AGE_INDICATION -> hasType(listOf(RANGE_START, RANGE_END), instance)
            Section.OFFICIAL_REFERENCE -> hasNone(instance)
            Section.PRINT_ANNOUNCEMENT -> hasType(listOf(ANNOUNCEMENT_GAZETTE, YEAR, NUMBER, PAGE, ADDITIONAL_INFO, EXPLANATION), instance)
            Section.DIGITAL_ANNOUNCEMENT -> hasType(listOf(ANNOUNCEMENT_MEDIUM, DATE, YEAR, EDITION, PAGE, AREA_OF_PUBLICATION, NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA, ADDITIONAL_INFO, EXPLANATION), instance)
            Section.EU_ANNOUNCEMENT -> hasType(listOf(EU_GOVERNMENT_GAZETTE, YEAR, SERIES, NUMBER, PAGE, ADDITIONAL_INFO, EXPLANATION), instance)
            Section.OTHER_OFFICIAL_ANNOUNCEMENT -> hasType(listOf(OTHER_OFFICIAL_REFERENCE), instance)
            Section.NORM_PROVIDER -> hasType(listOf(ENTITY, DECIDING_BODY, RESOLUTION_MAJORITY), instance)
            Section.DOCUMENT_TYPE -> hasType(listOf(TYPE_NAME, NORM_CATEGORY, TEMPLATE_NAME), instance)
            Section.DIVERGENT_ENTRY_INTO_FORCE -> hasNone(instance)
            Section.DIVERGENT_ENTRY_INTO_FORCE_DEFINED -> hasType(listOf(DATE, NORM_CATEGORY), instance)
            Section.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED -> hasType(listOf(UNDEFINED_DATE, NORM_CATEGORY), instance)
            Section.DIVERGENT_EXPIRATION -> hasNone(instance)
            Section.DIVERGENT_EXPIRATION_DEFINED -> hasType(listOf(DATE, NORM_CATEGORY), instance)
            Section.DIVERGENT_EXPIRATION_UNDEFINED -> hasType(listOf(UNDEFINED_DATE, NORM_CATEGORY), instance)
            Section.CATEGORIZED_REFERENCE -> hasType(listOf(TEXT), instance)
            Section.ENTRY_INTO_FORCE -> hasOneOfType(listOf(DATE, UNDEFINED_DATE), instance)
            Section.PRINCIPLE_ENTRY_INTO_FORCE -> hasOneOfType(listOf(DATE, UNDEFINED_DATE), instance)
            Section.EXPIRATION -> hasOneOfType(listOf(DATE, UNDEFINED_DATE), instance)
            Section.PRINCIPLE_EXPIRATION -> hasOneOfType(listOf(DATE, UNDEFINED_DATE), instance)
            Section.DIGITAL_EVIDENCE -> hasType(listOf(LINK, RELATED_DATA, EXTERNAL_DATA_NOTE, APPENDIX), instance)
            Section.FOOTNOTES -> hasType(listOf(FOOTNOTE_REFERENCE, FOOTNOTE_CHANGE, FOOTNOTE_COMMENT, FOOTNOTE_DECISION, FOOTNOTE_STATE_LAW, FOOTNOTE_EU_LAW, FOOTNOTE_OTHER), instance)
            Section.DOCUMENT_STATUS_SECTION -> hasNone(instance)
            Section.DOCUMENT_STATUS -> hasType(listOf(WORK_NOTE, DESCRIPTION, DATE, YEAR, REFERENCE, ENTRY_INTO_FORCE_DATE_NOTE, PROOF_INDICATION), instance)
            Section.DOCUMENT_TEXT_PROOF -> hasType(listOf(PROOF_TYPE, TEXT), instance)
            Section.DOCUMENT_OTHER -> hasType(listOf(OTHER_TYPE), instance)
            Section.STATUS_INDICATION -> hasNone(instance)
            Section.STATUS -> hasType(listOf(NOTE, DESCRIPTION, DATE, REFERENCE), instance)
            Section.REISSUE -> hasType(listOf(NOTE, ARTICLE, DATE, REFERENCE), instance)
            Section.REPEAL -> hasType(listOf(TEXT), instance)
            Section.OTHER_STATUS -> hasType(listOf(NOTE), instance)
            Section.PUBLICATION_DATE -> hasOneOfType(listOf(DATE, YEAR), instance)
            Section.ANNOUNCEMENT_DATE -> hasOneCombination(listOf(listOf(DATE, TIME), listOf(YEAR)), instance)
        }

        private fun hasNone(instance: MetadataSection): Boolean =
            instance.metadata.isEmpty()
        private fun hasType(types: List<MetadatumType>, instance: MetadataSection): Boolean =
            instance.metadata.all { it.type in types }
        private fun hasOneOfType(types: List<MetadatumType>, instance: MetadataSection): Boolean =
            instance.metadata.count() == 1 && hasType(types, instance)
        private fun hasOneCombination(combinations: List<List<MetadatumType>>, instance: MetadataSection): Boolean =
            combinations.map { combination -> hasType(combination, instance) }.count { true } == 1
    }
