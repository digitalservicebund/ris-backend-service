package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.schema

import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.NormCategory
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.decodeLocalDate
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.decodeLocalTime

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
