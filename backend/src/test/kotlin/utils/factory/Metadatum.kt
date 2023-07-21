package utils.factory

import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import utils.randomString
import java.util.UUID

fun metadatum(block: MetadatumBuilder.() -> Unit): Metadatum<*> = MetadatumBuilder().apply(block).build()

class MetadatumBuilder {
    var value: Any = randomString()
    var type: MetadatumType = MetadatumType.LEAD_UNIT
    var order: Int = 1
    var guid: UUID = UUID.randomUUID()

    fun build(): Metadatum<*> = Metadatum(value, type, order, guid)
}
