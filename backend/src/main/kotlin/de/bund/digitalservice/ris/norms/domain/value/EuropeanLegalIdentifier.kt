package de.bund.digitalservice.ris.norms.domain.value

import java.time.LocalDate

data class EuropeanLegalIdentifier(
    val printAnnouncementGazette: String?,
    val announcementDate: LocalDate?,
    val citationDate: LocalDate?,
    val printAnnouncementPage: String?
) {
    companion object {
        fun parseGazette(gazette: String?): String? = when (gazette) {
            "bgbl-1" -> "BGBl I"
            "bgbl-2" -> "BGBl II"
            "banz-at" -> "BAnz"
            else -> gazette
        }
    }
    private val year: Int? = announcementDate?.year ?: citationDate?.year
    private val gazette: String? = when (printAnnouncementGazette) {
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
