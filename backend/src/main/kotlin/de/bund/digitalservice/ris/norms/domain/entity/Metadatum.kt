package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.specification.metadatum.hasValidValueType

data class Metadatum<T>(val value: T, val type: MetadatumType, val order: Int = 0) {
    init {
        require(hasValidValueType.isSatisfiedBy(this)) {
            "Incorrect value type '${value!!::class.java.simpleName}' for datum type '$type'"
        }
    }
}

enum class MetadatumType() {
    KEYWORD,
    UNOFFICIAL_LONG_TITLE,
    UNOFFICIAL_SHORT_TITLE,
    UNOFFICIAL_ABBREVIATION,
    UNOFFICIAL_REFERENCE,
    DIVERGENT_DOCUMENT_NUMBER,
    REFERENCE_NUMBER,
    DEFINITION,
    RIS_ABBREVIATION_INTERNATIONAL_LAW,
    AGE_OF_MAJORITY_INDICATION,
    VALIDITY_RULE,
}
