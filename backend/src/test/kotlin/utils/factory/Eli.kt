package utils.factory

import de.bund.digitalservice.ris.norms.domain.value.Eli
import java.time.LocalDate

fun eli(block: EliBuilder.() -> Unit): Eli = EliBuilder().apply(block).build()

class EliBuilder {
    var printAnnouncementGazette: String? = null
    var digitalAnnouncementMedium: String? = null
    var announcementYear: Int? = null
    var citationDate: LocalDate? = LocalDate.now()
    var citationYear: String? = null
    var printAnnouncementPage: String? = null
    var digitalAnnouncementPage: String? = null
    var digitalAnnouncementEdition: String? = null

    fun build(): Eli = Eli(printAnnouncementGazette, digitalAnnouncementMedium, announcementYear, citationDate, citationYear, printAnnouncementPage, digitalAnnouncementPage, digitalAnnouncementEdition)
}
