package de.bund.digitalservice.ris.norms.application.port.input

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LoadNormAsXmlQueryTest {
    @Test
    fun `can create query with GUID`() {
        val printAnnouncementGazette = "bg-1"
        val publicationYear = "2022"
        val printAnnouncementPage = "1125"
        val query = LoadNormAsXmlUseCase.Query(printAnnouncementGazette, publicationYear, printAnnouncementPage)

        assertThat(query.printAnnouncementGazette).isEqualTo(printAnnouncementGazette)
        assertThat(query.publicationYear).isEqualTo(publicationYear)
        assertThat(query.printAnnouncementPage).isEqualTo(printAnnouncementPage)
    }
}
