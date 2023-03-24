package de.bund.digitalservice.ris.norms.domain.specification.metadatum

import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.MetadatumType.AGE_OF_MAJORITY_INDICATION
import de.bund.digitalservice.ris.norms.domain.entity.MetadatumType.DEFINITION
import de.bund.digitalservice.ris.norms.domain.entity.MetadatumType.DIVERGENT_DOCUMENT_NUMBER
import de.bund.digitalservice.ris.norms.domain.entity.MetadatumType.KEYWORD
import de.bund.digitalservice.ris.norms.domain.entity.MetadatumType.REFERENCE_NUMBER
import de.bund.digitalservice.ris.norms.domain.entity.MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW
import de.bund.digitalservice.ris.norms.domain.entity.MetadatumType.UNOFFICIAL_ABBREVIATION
import de.bund.digitalservice.ris.norms.domain.entity.MetadatumType.UNOFFICIAL_LONG_TITLE
import de.bund.digitalservice.ris.norms.domain.entity.MetadatumType.UNOFFICIAL_REFERENCE
import de.bund.digitalservice.ris.norms.domain.entity.MetadatumType.UNOFFICIAL_SHORT_TITLE
import de.bund.digitalservice.ris.norms.domain.entity.MetadatumType.VALIDITY_RULE
import de.bund.digitalservice.ris.norms.domain.specification.Specification

val hasValidValueType =
    object : Specification<Metadatum<*>> {
        override fun isSatisfiedBy(instance: Metadatum<*>): Boolean {
            return when (instance.type) {
                KEYWORD, UNOFFICIAL_LONG_TITLE, UNOFFICIAL_SHORT_TITLE, UNOFFICIAL_ABBREVIATION, UNOFFICIAL_REFERENCE, DIVERGENT_DOCUMENT_NUMBER, REFERENCE_NUMBER, DEFINITION, RIS_ABBREVIATION_INTERNATIONAL_LAW, AGE_OF_MAJORITY_INDICATION, VALIDITY_RULE -> instance.value is String
            }
        }
    }
