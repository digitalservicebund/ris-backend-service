package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.schema

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName

class MetadataSectionRequestSchema {
  lateinit var name: MetadataSectionName
  var order: Int = 1
  var metadata: List<MetadatumRequestSchema> = emptyList()
  var sections: List<MetadataSectionRequestSchema>? = null

  fun toUseCaseData(): MetadataSection {
    val childSections = this.sections?.map { it.toUseCaseData() }
    return MetadataSection(
        name = this.name,
        order = order,
        metadata = metadata.map(MetadatumRequestSchema::toUseCaseData),
        sections = childSections,
    )
  }
}
