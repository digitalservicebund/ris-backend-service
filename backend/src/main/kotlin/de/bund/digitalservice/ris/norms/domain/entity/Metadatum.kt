package de.bund.digitalservice.ris.norms.domain.entity

data class Metadatum<T>(val value: T, val type: MetadatumType, val order: Int = 0)

enum class MetadatumType() {
    KEYWORD,
}
