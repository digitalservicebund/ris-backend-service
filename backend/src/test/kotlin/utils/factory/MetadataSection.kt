package utils.factory

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName

fun metadataSection(block: MetadataSectionBuilder.() -> Unit): MetadataSection = MetadataSectionBuilder().apply(block).build()

class MetadataSectionBuilder {
    var name: MetadataSectionName = MetadataSectionName.LEAD
    var order: Int = 1
    private val metadata = mutableListOf<Metadatum<*>>()
    private val sections = mutableListOf<MetadataSection>()

    fun metadata(block: Metadata.() -> Unit) = metadata.addAll(Metadata().apply(block))
    fun sections(block: MetadataSections.() -> Unit) = sections.addAll(MetadataSections().apply(block))

    fun build(): MetadataSection = MetadataSection(name, metadata, order, sections)
}

class Metadata : ArrayList<Metadatum<*>>() {
    fun metadatum(block: MetadatumBuilder.() -> Unit) = add(MetadatumBuilder().apply(block).build())
}
