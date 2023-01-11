package de.bund.digitalservice.ris.norms.application.port.input

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LoadNormAsXmlQueryTest {
    @Test
    fun `can create query with GUID`() {
        val printAnnouncementGazette = "bg-1"
        val announcementOrCitationYear = "2022"
        val printAnnouncementPage = "1125"
        val query = LoadNormAsXmlUseCase.Query(printAnnouncementGazette, announcementOrCitationYear, printAnnouncementPage)

        assertThat(query.printAnnouncementGazette).isEqualTo(printAnnouncementGazette)
        assertThat(query.announcementOrCitationYear).isEqualTo(announcementOrCitationYear)
        assertThat(query.printAnnouncementPage).isEqualTo(printAnnouncementPage)
    }
}
