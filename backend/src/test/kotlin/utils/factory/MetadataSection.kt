package utils.factory

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import java.util.*
import kotlin.collections.ArrayList

fun metadataSection(block: MetadataSectionBuilder.() -> Unit): MetadataSection =
    MetadataSectionBuilder().apply(block).build()

class MetadataSectionBuilder {
  var guid: UUID = UUID.randomUUID()
  var name: MetadataSectionName = MetadataSectionName.LEAD
  var order: Int = 1
  private val metadata = mutableListOf<Metadatum<*>>()
  private var sections: List<MetadataSection>? = null

  fun metadata(block: Metadata.() -> Unit) = metadata.addAll(Metadata().apply(block))

  fun sections(block: MetadataSections.() -> Unit) {
    val oldSections = this.sections
    val newSections = MetadataSections().apply(block)

    if (oldSections == null) sections = newSections else sections = oldSections + newSections
  }

  fun build(): MetadataSection = MetadataSection(name, metadata, order, sections, guid)
}

class Metadata : ArrayList<Metadatum<*>>() {
  fun metadatum(block: MetadatumBuilder.() -> Unit) = add(MetadatumBuilder().apply(block).build())
}
