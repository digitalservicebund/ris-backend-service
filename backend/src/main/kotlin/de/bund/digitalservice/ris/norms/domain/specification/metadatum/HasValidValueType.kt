package de.bund.digitalservice.ris.norms.domain.specification.metadatum

import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.RangeUnit
import de.bund.digitalservice.ris.norms.domain.specification.Specification
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
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.OTHER_OFFICIAL_REFERENCES
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
import java.time.LocalDate

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
                NUMBER, PAGE_NUMBER, ADDITIONAL_INFO, EXPLANATION, ANNOUNCEMENT_MEDIUM,
                AREA_OF_PUBLICATION, NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA,
                EU_GOVERNMENT_GAZETTE, SERIES, OTHER_OFFICIAL_REFERENCES,
                -> instance.value is String

                RANGE_START_UNIT, RANGE_END_UNIT -> instance.value is RangeUnit

                DATE -> instance.value is LocalDate
            }
        }
    }
