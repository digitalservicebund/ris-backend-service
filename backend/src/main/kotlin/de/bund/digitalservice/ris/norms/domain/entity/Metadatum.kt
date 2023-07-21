package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.specification.metadatum.hasValidValueType
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import java.util.UUID

data class Metadatum<T : Any>(val value: T, val type: MetadatumType, val order: Int = 1, val guid: UUID = UUID.randomUUID()) {
    init {
        require(hasValidValueType.isSatisfiedBy(this)) {
            "Incorrect value type '${value::class.java.simpleName}' for datum type '$type'"
        }
    }
}
