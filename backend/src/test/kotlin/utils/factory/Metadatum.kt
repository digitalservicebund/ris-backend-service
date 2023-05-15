package utils.factory

import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import utils.randomString

fun metadatum(block: MetadatumBuilder.() -> Unit): Metadatum<*> = MetadatumBuilder().apply(block).build()

class MetadatumBuilder {
    var value: String? = randomString()
    var type: MetadatumType = MetadatumType.LEAD_UNIT

    fun build(): Metadatum<*> = Metadatum(value, type)
}
