package de.bund.digitalservice.ris.norms.domain.value

import java.util.UUID

data class Guid private constructor(val identifier: UUID) {
    companion object {
        fun generateNew(): Guid {
            return Guid(UUID.randomUUID())
        }

        fun fromString(identifier: String): Guid {
            return Guid(UUID.fromString(identifier))
        }
    }

    override fun toString(): String {
        return identifier.toString()
    }
}
