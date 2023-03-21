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
}
