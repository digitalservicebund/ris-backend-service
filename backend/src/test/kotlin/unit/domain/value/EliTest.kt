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
        )
        assertThat(eli.toString()).isEqualTo("")
    }

    @Test
    fun `eli returns the mapped value for gazette when converted to string`() {
        val eli = Eli(
            "BAnz",
            LocalDate.now(),
            LocalDate.now(),
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
            "111",
        )
        assertThat(eli.toString()).isEqualTo("")
    }

    @Test
    fun `gazette value can be mapped back to its original juris value`() {
        val gazette = Eli.parseGazette("banz-at")
        assertThat(gazette).isEqualTo("BAnz")
    }
}
