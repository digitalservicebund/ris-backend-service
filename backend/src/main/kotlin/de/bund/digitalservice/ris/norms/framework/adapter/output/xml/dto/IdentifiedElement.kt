package de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto

import java.util.UUID

data class IdentifiedElement(
    val value: String?,
    val guid: UUID = UUID.randomUUID(),
    val childGuid: UUID = UUID.randomUUID()
)
