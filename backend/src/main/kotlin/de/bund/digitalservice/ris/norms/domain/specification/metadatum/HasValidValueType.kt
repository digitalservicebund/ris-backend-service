package de.bund.digitalservice.ris.norms.domain.specification.metadatum

import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.specification.Specification
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
import de.bund.digitalservice.ris.norms.domain.value.NormCategory
import de.bund.digitalservice.ris.norms.domain.value.OtherType
import de.bund.digitalservice.ris.norms.domain.value.ProofIndication
import de.bund.digitalservice.ris.norms.domain.value.ProofType
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import java.time.LocalDate
import java.time.LocalTime

val hasValidValueType =
    object : Specification<Metadatum<*>> {
        override fun isSatisfiedBy(instance: Metadatum<*>): Boolean {
            return when (instance.type) {
                KEYWORD, UNOFFICIAL_LONG_TITLE, UNOFFICIAL_SHORT_TITLE, UNOFFICIAL_ABBREVIATION,
                UNOFFICIAL_REFERENCE, DIVERGENT_DOCUMENT_NUMBER, REFERENCE_NUMBER, DEFINITION,
                RIS_ABBREVIATION_INTERNATIONAL_LAW, AGE_OF_MAJORITY_INDICATION,
                VALIDITY_RULE, LEAD_JURISDICTION, LEAD_UNIT, PARTICIPATION_TYPE,
                PARTICIPATION_INSTITUTION, SUBJECT_FNA, SUBJECT_PREVIOUS_FNA,
                SUBJECT_GESTA, SUBJECT_BGB_3, YEAR, RANGE_START, RANGE_END, ANNOUNCEMENT_GAZETTE,
                NUMBER, PAGE, ADDITIONAL_INFO, EXPLANATION, ANNOUNCEMENT_MEDIUM,
                AREA_OF_PUBLICATION, NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA,
                SERIES, DECIDING_BODY, EU_GOVERNMENT_GAZETTE, OTHER_OFFICIAL_REFERENCE, ENTITY,
                TYPE_NAME, TEMPLATE_NAME, EDITION, TEXT, LINK, RELATED_DATA, EXTERNAL_DATA_NOTE, APPENDIX, FOOTNOTE_REFERENCE, FOOTNOTE_CHANGE,
                FOOTNOTE_COMMENT, FOOTNOTE_DECISION, FOOTNOTE_STATE_LAW, FOOTNOTE_EU_LAW, FOOTNOTE_OTHER,
                WORK_NOTE, DESCRIPTION, REFERENCE, ENTRY_INTO_FORCE_DATE_NOTE, NOTE, ARTICLE, OFFICIAL_LONG_TITLE,
                OFFICIAL_SHORT_TITLE, OFFICIAL_ABBREVIATION, DOCUMENT_NUMBER, DOCUMENT_CATEGORY, COMPLETE_CITATION,
                CELEX_NUMBER, RIS_ABBREVIATION, TIME,
                -> instance.value is String

                DATE -> instance.value is LocalDate

                NORM_CATEGORY -> instance.value is NormCategory

                RESOLUTION_MAJORITY -> instance.value is Boolean

                UNDEFINED_DATE -> instance.value is UndefinedDate

                PROOF_INDICATION -> instance.value is ProofIndication

                PROOF_TYPE -> instance.value is ProofType

                OTHER_TYPE -> instance.value is OtherType

                TIME -> instance.value is LocalTime
            }
        }
    }
