package de.bund.digitalservice.ris.norms.domain.value

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.factory.eli
import java.time.LocalDate

class EliTest {
    @Test
    fun `eli returns an empty string if print announcement is not complete and digital announcement does not exist`() {
        val eli = eli { printAnnouncementGazette = "printAnnouncementGazette" }
        assertThat(eli.toString()).isEqualTo("")
    }

    @Test
    fun `eli returns an empty string if print announcement gazette does not exist but print announcement number exist and and digital announcement doesn't exist`() {
        val eli = eli { printAnnouncementPage = "111" }
        assertThat(eli.toString()).isEqualTo("")
    }

    @Test
    fun `eli returns an empty string if print announcement gazette does not exist and print announcement page exist and and digital announcement medium exists but not edition or page`() {
        val eli = eli { digitalAnnouncementMedium = "digitalAnnouncementMedium"; printAnnouncementPage = "111" }
        assertThat(eli.toString()).isEqualTo("")
    }

    @Test
    fun `eli returns empty string if the gazette is passed as empty string`() {
        val eli = eli { printAnnouncementGazette = ""; printAnnouncementPage = "111" }
        assertThat(eli.toString()).isEqualTo("")
    }

    @Test
    fun `eli returns the mapped value for gazette when converted to string`() {
        val eli = eli { printAnnouncementGazette = "BAnz"; printAnnouncementPage = "111" }
        assertThat(eli.toString()).isEqualTo("eli/banz-at/${LocalDate.now().year}/s111")
    }

    @Test
    fun `eli returns the same value for medium if no mapped result found when converted to string`() {
        val eli = eli { digitalAnnouncementMedium = "noMappingValue"; digitalAnnouncementPage = "111"; printAnnouncementPage = "222" }
        assertThat(eli.toString()).isEqualTo("eli/noMappingValue/${LocalDate.now().year}/s111")
    }

    @Test
    fun `eli returns a string with digital announcement medium and edition even if digital and print announcement pages exists and print announcement is null`() {
        val eli = eli { digitalAnnouncementMedium = "BGBl I"; digitalAnnouncementEdition = "111"; digitalAnnouncementPage = "333"; printAnnouncementPage = "222" }
        assertThat(eli.toString()).isEqualTo("eli/bgbl-1/${LocalDate.now().year}/s111")
    }

    @Test
    fun `eli returns a string with digital announcement medium and page if print announcement gazette exists but not the page and if digital announcement edition is empty`() {
        val eli = eli { digitalAnnouncementMedium = "BGBl I"; digitalAnnouncementMedium = "BGBl II"; digitalAnnouncementPage = "333" }
        assertThat(eli.toString()).isEqualTo("eli/bgbl-2/${LocalDate.now().year}/s333")
    }

    @Test
    fun `gazette value can be mapped back to its original juris value`() {
        val gazette = Eli.parseGazette("banz-at")
        assertThat(gazette).isEqualTo("BAnz")
    }

    @Test
    fun `returns an eli with the year of the announcement date having all dates filled`() {
        val eli = eli {
            printAnnouncementGazette = "BGBl I"
            announcementYear = 1989
            citationDate = LocalDate.of(1988, 6, 9)
            citationYear = "2001"
            printAnnouncementPage = "1234"
        }
        assertThat(eli.toString()).isEqualTo("eli/bgbl-1/1989/s1234")
    }

    @Test
    fun `returns an eli with the year of the announcement date having also only the citation date`() {
        val eli = eli {
            printAnnouncementGazette = "BGBl I"
            announcementYear = 1989
            citationDate = LocalDate.of(1988, 6, 9)
            printAnnouncementPage = "1234"
        }
        assertThat(eli.toString()).isEqualTo("eli/bgbl-1/1989/s1234")
    }

    @Test
    fun `returns an eli with the year of the announcement date having also only the citation year`() {
        val eli = eli {
            printAnnouncementGazette = "BGBl I"
            announcementYear = 1989
            citationYear = "2001"
            printAnnouncementPage = "1234"
        }
        assertThat(eli.toString()).isEqualTo("eli/bgbl-1/1989/s1234")
    }

    @Test
    fun `returns an eli with the year of the citation date`() {
        val eli = eli {
            printAnnouncementGazette = "BGBl I"
            announcementYear = null
            citationDate = LocalDate.of(1988, 6, 9)
            citationYear = "2001"
            printAnnouncementPage = "1234"
        }
        assertThat(eli.toString()).isEqualTo("eli/bgbl-1/1988/s1234")
    }

    @Test
    fun `returns an eli with the year of the citation year`() {
        val eli = eli {
            printAnnouncementGazette = "BGBl I"
            announcementYear = null
            citationDate = null
            citationYear = "2001"
            printAnnouncementPage = "1234"
        }
        assertThat(eli.toString()).isEqualTo("eli/bgbl-1/2001/s1234")
    }
}
