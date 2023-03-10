package de.bund.digitalservice.ris.norms.domain.value

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class EliTest {
    @Test
    fun `eli returns an empty string if one of the parameters is null`() {
        val eli = Eli(
            "printAnnouncementGazette",
            LocalDate.now(),
            LocalDate.now(),
            null,
            null,
        )
        assertThat(eli.toString()).isEqualTo("")
    }

    @Test
    fun `eli returns the mapped value for gazette when converted to string`() {
        val eli = Eli(
            "BAnz",
            LocalDate.now(),
            LocalDate.now(),
            null,
            "111",
        )
        assertThat(eli.toString()).isEqualTo("eli/banz-at/${LocalDate.now().year}/s111")
    }

    @Test
    fun `eli returns empty string if the gazette is passed as empty string`() {
        val eli = Eli(
            "",
            LocalDate.now(),
            LocalDate.now(),
            null,
            "111",
        )
        assertThat(eli.toString()).isEqualTo("")
    }

    @Test
    fun `gazette value can be mapped back to its original juris value`() {
        val gazette = Eli.parseGazette("banz-at")
        assertThat(gazette).isEqualTo("BAnz")
    }

    @Test
    fun `returns an eli with the year of the announcement date having all dates filled`() {
        val eli = Eli(
            "BGBl I",
            LocalDate.of(1989, 12, 29),
            LocalDate.of(1988, 6, 9),
            "2001",
            "1234",
        )
        assertThat(eli.toString()).isEqualTo("eli/bgbl-1/1989/s1234")
    }

    @Test
    fun `returns an eli with the year of the announcement date having also only the citation date`() {
        val eli = Eli(
            "BGBl I",
            LocalDate.of(1989, 12, 29),
            LocalDate.of(1988, 6, 9),
            null,
            "1234",
        )
        assertThat(eli.toString()).isEqualTo("eli/bgbl-1/1989/s1234")
    }

    @Test
    fun `returns an eli with the year of the announcement date having also only the citation year`() {
        val eli = Eli(
            "BGBl I",
            LocalDate.of(1989, 12, 29),
            null,
            "2001",
            "1234",
        )
        assertThat(eli.toString()).isEqualTo("eli/bgbl-1/1989/s1234")
    }

    @Test
    fun `returns an eli with the year of the citation date`() {
        val eli = Eli(
            "BGBl I",
            null,
            LocalDate.of(1988, 6, 9),
            "2001",
            "1234",
        )
        assertThat(eli.toString()).isEqualTo("eli/bgbl-1/1988/s1234")
    }

    @Test
    fun `returns an eli with the year of the citation year`() {
        val eli = Eli(
            "BGBl I",
            null,
            null,
            "2001",
            "1234",
        )
        assertThat(eli.toString()).isEqualTo("eli/bgbl-1/2001/s1234")
    }
}
