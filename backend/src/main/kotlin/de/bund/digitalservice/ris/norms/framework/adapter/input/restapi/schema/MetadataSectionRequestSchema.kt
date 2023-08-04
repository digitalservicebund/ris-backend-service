package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.schema

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.NormCategory
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.decodeLocalDate
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.decodeLocalTime

class MetadataSectionRequestSchema {
  lateinit var name: MetadataSectionName
  var metadata: List<MetadatumRequestSchema>? = null
  var order: Int = 1
  var sections: List<MetadataSectionRequestSchema>? = null

  fun toUseCaseData(): MetadataSection {
    val metadata = this.metadata?.map { it.toUseCaseData() }
    val childSections = this.sections?.map { it.toUseCaseData() }
    return MetadataSection(
        name = this.name,
        order = order,
        metadata = metadata ?: emptyList(),
        sections = childSections)
  }
}

class MetadatumRequestSchema {
  lateinit var value: String
  lateinit var type: MetadatumType
  var order: Int = 1

  fun toUseCaseData(): Metadatum<*> {
    val value =
        when (this.type) {
          MetadatumType.DATE -> decodeLocalDate(this.value)
          MetadatumType.TIME -> decodeLocalTime(this.value)
          MetadatumType.RESOLUTION_MAJORITY -> this.value.toBoolean()
          MetadatumType.NORM_CATEGORY -> NormCategory.valueOf(this.value)
          MetadatumType.UNDEFINED_DATE -> UndefinedDate.valueOf(this.value)
          else -> this.value
        }
    return Metadatum(value = value, type = this.type, order = this.order)
  }
}
