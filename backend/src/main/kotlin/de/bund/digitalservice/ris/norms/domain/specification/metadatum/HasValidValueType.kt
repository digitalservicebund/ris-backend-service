package de.bund.digitalservice.ris.norms.domain.specification.metadatum

import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.MetadatumType.KEYWORD
import de.bund.digitalservice.ris.norms.domain.specification.Specification

val hasValidValueType =
    object : Specification<Metadatum<*>> {
        override fun isSatisfiedBy(instance: Metadatum<*>): Boolean {
            return when (instance.type) {
                KEYWORD -> String::class.isInstance(instance.value)
            }
        }
    }
