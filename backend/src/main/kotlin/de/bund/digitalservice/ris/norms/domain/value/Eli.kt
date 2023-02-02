package de.bund.digitalservice.ris.norms.domain.value

import java.time.LocalDate

/**
 * The acronym ELI stands for European Legislation Identifier, which is a system used to standardise the format with
 * which a legislation can be accessed online. In our application it is a computed value built from the properties
 * passed within the primary constructor.
 *
 * @property printAnnouncementGazette the printed gazette on which the norm was announced.
 * @property announcementDate the date on which the norm was announced.
 * @property citationDate the date on which the norm was citated.
 * @property printAnnouncementPage the printed page number on which the norm was announced.
 */
data class Eli(
    val printAnnouncementGazette: String?,
    val announcementDate: LocalDate?,
    val citationDate: LocalDate?,
    val printAnnouncementPage: String?,
) {
    companion object {
        fun parseGazette(gazette: String): String = when (gazette) {
            "bgbl-1" -> "BGBl I"
            "bgbl-2" -> "BGBl II"
            "banz-at" -> "BAnz"
            else -> gazette
        }
    }
    private val year: Int? = announcementDate?.year ?: citationDate?.year
    val gazette: String? = when (printAnnouncementGazette) {
        "BGBl I" -> "bgbl-1"
        "BGBl II" -> "bgbl-2"
        "BAnz" -> "banz-at"
        "" -> null
        else -> printAnnouncementGazette
    }

    override fun toString(): String {
        return if (year == null || gazette == null || printAnnouncementPage == null) {
            ""
        } else {
            "eli/$gazette/$year/s$printAnnouncementPage"
        }
    }
}
