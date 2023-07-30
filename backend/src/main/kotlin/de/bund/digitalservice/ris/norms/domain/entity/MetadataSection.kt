package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.specification.section.hasValidChildren
import de.bund.digitalservice.ris.norms.domain.specification.section.hasValidMetadata
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import java.util.*

data class MetadataSection(
    val name: MetadataSectionName,
    val metadata: List<Metadatum<*>>,
    val order: Int = 1,
    val sections: List<MetadataSection>? = null,
    val guid: UUID = UUID.randomUUID(),
) {
  init {
    hasValidChildren.evaluate(this).throwWhenUnsatisfied()
    hasValidMetadata.evaluate(this).throwWhenUnsatisfied()
  }
}
