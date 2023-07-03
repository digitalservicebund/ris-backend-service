package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.specification.norm.hasValidSections
import de.bund.digitalservice.ris.norms.domain.value.Eli
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import java.time.LocalDate
import java.util.UUID

data class Norm(
    val guid: UUID,
    val articles: List<Article> = emptyList(),
    val metadataSections: List<MetadataSection> = emptyList(),

    var announcementDate: LocalDate? = null,

    var files: List<FileReference> = listOf(),

) {
    init {
        require(hasValidSections.isSatisfiedBy(this)) {
            "Incorrect section for metadata"
        }
    }
    val eli: Eli
        get() =
            Eli(
                digitalAnnouncementMedium = getFirstMetadatum(MetadataSectionName.DIGITAL_ANNOUNCEMENT, MetadatumType.ANNOUNCEMENT_MEDIUM, MetadataSectionName.OFFICIAL_REFERENCE)?.let { it.value as String },
                printAnnouncementGazette = getFirstMetadatum(MetadataSectionName.PRINT_ANNOUNCEMENT, MetadatumType.ANNOUNCEMENT_GAZETTE, MetadataSectionName.OFFICIAL_REFERENCE)?.let { it.value as String },
                announcementYear = announcementYear,
                citationDate = getFirstMetadatum(MetadataSectionName.CITATION_DATE, MetadatumType.DATE)?.let { it.value as LocalDate },
                citationYear = getFirstMetadatum(MetadataSectionName.CITATION_DATE, MetadatumType.YEAR)?.let { it.value as String },
                printAnnouncementPage = getFirstMetadatum(MetadataSectionName.PRINT_ANNOUNCEMENT, MetadatumType.PAGE, MetadataSectionName.OFFICIAL_REFERENCE)?.let { it.value as String },
                digitalAnnouncementEdition = getFirstMetadatum(MetadataSectionName.DIGITAL_ANNOUNCEMENT, MetadatumType.EDITION, MetadataSectionName.OFFICIAL_REFERENCE)?.let { it.value as String },
                digitalAnnouncementPage = getFirstMetadatum(MetadataSectionName.DIGITAL_ANNOUNCEMENT, MetadatumType.PAGE, MetadataSectionName.OFFICIAL_REFERENCE)?.let { it.value as String },
            )

    private val announcementYear: Int?
        get() {
            val date = getFirstMetadatum(MetadataSectionName.ANNOUNCEMENT_DATE, MetadatumType.DATE)
            val dateValue = (date?.value as LocalDate?)?.year

            val year = getFirstMetadatum(MetadataSectionName.ANNOUNCEMENT_DATE, MetadatumType.YEAR)
            val yearValue = (year?.value as String?)?.toInt()

            return dateValue ?: yearValue ?: announcementDate?.year?.toInt()
        }

    fun getFirstMetadatum(section: MetadataSectionName, type: MetadatumType, parent: MetadataSectionName? = null): Metadatum<*>? =
        when (parent) {
            null -> metadataSections
            else -> metadataSections.filter { it.name == parent }.flatMap { it.sections ?: listOf() }
        }.filter { it.name == section }
            .minByOrNull { it.order }
            ?.let {
                it.metadata.filter { metadatum -> metadatum.type == type }.minByOrNull { metadatum -> metadatum.order }
            }
}
