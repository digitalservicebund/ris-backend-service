package de.bund.digitalservice.ris.norms.domain.value

import java.time.LocalDate

/**
 * The acronym ELI stands for European Legislation Identifier, which is a system used to standardise the format with
 * which a legislation can be accessed online. In our application it is a computed value built from the properties
 * passed within the primary constructor.
 *
 * The ELI is constructed via the `toString()` function of this class, which builds a string representation of the ELI identifier by concatenating the values of `gazetteOrMedium`, `year`, and `page` properties.
 * The `gazetteOrMedium` property is computed based on the `printAnnouncementGazette` and `digitalAnnouncementMedium` metadata of the norm.
 * If `printAnnouncementGazette` is not null, then the value of `gazetteOrMedium` is the mapped value of `printAnnouncementGazette`, otherwise, the `digitalAnnouncementMedium` is used
 * If both `printAnnouncementGazette` and `digitalAnnouncementMedium` are null, then the value of `gazetteOrMedium` is also null.
 * The `year` property is computed based on the `announcementDate`, `citationDate`, and `citationYear` metadata of the norm.
 * If `announcementDate` is not null, then the value of `year` is set to the year of the `announcementDate`. If `announcementDate` is null, then the value of `citationDate` is checked.
 * If `citationDate` is not null, then the value of `year` is set to the year of the `citationDate`. If both `announcementDate` and `citationDate` are null, then the value of `citationYear` is checked.
 * If `citationYear` is not null, then the value of `year` is set to the integer value of `citationYear`. If all three properties (`announcementDate`, `citationDate`, and `citationYear`) are null, then the value of `year` is also null.
 * The `page` property is computed based on the `printAnnouncementGazette`, `printAnnouncementPage`, `digitalAnnouncementEdition`, and `digitalAnnouncementPage` metadata of the norm.
 * If `printAnnouncementGazette` is not null, then the value of `page` is set to the value of `printAnnouncementPage`.
 * If `printAnnouncementGazette` is null, then the value of `digitalAnnouncementEdition` is checked. If `digitalAnnouncementEdition` is not null, then the value of `page` is set to the value of `digitalAnnouncementEdition`.
 * If `digitalAnnouncementEdition` is null, then the value of `digitalAnnouncementPage` is checked. If `digitalAnnouncementPage` is not null, then the value of `page` is set to the value of `digitalAnnouncementPage`.
 * If all four properties (`printAnnouncementGazette`, `printAnnouncementPage`, `digitalAnnouncementEdition`, and `digitalAnnouncementPage`) are null, then the value of `page` is also null.
 * The ELI string is returned as an empty string if all three properties (`gazetteOrMedium`, `year`, and `page`) are null, otherwise the eli is returned in this format: `eli/{gazetteOrMedium}/{year}/s{page}`.
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
    val announcementYear: Int?,
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
    private val year: Int? = announcementYear ?: (citationDate?.year ?: citationYear?.toInt())
    private val page: String? = if (printAnnouncementGazette != null) printAnnouncementPage else digitalAnnouncementEdition ?: digitalAnnouncementPage

    override fun toString() = if (year != null && gazetteOrMedium != null && page != null) "eli/$gazetteOrMedium/$year/s$page" else ""
}
