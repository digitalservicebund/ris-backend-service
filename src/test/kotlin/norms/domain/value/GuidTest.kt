package de.bund.digitalservice.ris.norms.domain.value

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class GuidTest {
    @Test
    fun `can generate a valid GUID`() {
        val guid = UUID.randomUUID()

        assertThat(guid).isNotNull()
        assertThat(guid.toString().length).isEqualTo(36)
        assertThat(guid.toString()).matches("[a-f0-9]{8}(?:-[a-f0-9]{4}){4}[a-f0-9]{8}")
    }

    @Test
    fun `can create instance from former generated identifer`() {
        val firstGuid = UUID.randomUUID()
        val secondGuid = UUID.fromString(firstGuid.toString())

        assertThat(secondGuid).isEqualTo(firstGuid)
    }
}
