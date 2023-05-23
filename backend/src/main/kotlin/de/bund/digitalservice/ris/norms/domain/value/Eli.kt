package de.bund.digitalservice.ris.norms.domain.value

import java.time.LocalDate

/**
 * The acronym ELI stands for European Legislation Identifier, which is a system used to standardise the format with
 * which a legislation can be accessed online. In our application it is a computed value built from the properties
 * passed within the primary constructor.
 *
 *
 * @property printAnnouncementGazette the printed gazette on which the norm was announced.
 * @property announcementDate the date on which the norm was announced.
 * @property citationDate the date on which the norm was citated.
 * @property citationYear the year on which the norm was citated.
 * @property printAnnouncementPage the printed page number on which the norm was announced.
 */
data class Eli(
    val printAnnouncementGazette: String?,
    val digitalAnnouncementMedium: String?,
    val announcementDate: LocalDate?,
    val citationDate: LocalDate?,
    val citationYear: String?,
    val printAnnouncementPage: String?,
    val digitalAnnouncementPage: String?,
    val digitalAnnouncementEdition: String?,
) {
    companion object {
        private val gazetteOrMediumMap = mapOf(
            "BGBl I" to "bgbl-1",
            "BGBl II" to "bgbl-2",
            "BAnz" to "banz-at",
            "" to null,
        )
        fun parseGazette(gazette: String): String = gazetteOrMediumMap.filterValues { it == gazette }.keys.firstOrNull() ?: gazette
    }

    val gazetteOrMedium: String? = printAnnouncementGazette?.let { gazetteOrMediumMap.getOrDefault(it, it) } ?: digitalAnnouncementMedium?.let { gazetteOrMediumMap.getOrDefault(it, it) }
    private val year: Int? = announcementDate?.year ?: (citationDate?.year ?: citationYear?.toInt())
    private val page: String? = if (printAnnouncementGazette != null) printAnnouncementPage else digitalAnnouncementEdition ?: digitalAnnouncementPage

    override fun toString() = if (year != null && gazetteOrMedium != null && page != null) "eli/$gazetteOrMedium/$year/s$page" else ""
}
