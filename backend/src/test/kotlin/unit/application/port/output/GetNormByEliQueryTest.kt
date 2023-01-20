package de.bund.digitalservice.ris.norms.application.port.output

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GetNormByEliQueryTest {

    @Test
    fun `can create query with all required fields`() {
        val query = GetNormByEliOutputPort.Query("bgbl-1", "2022", "1125")

        assertThat(query.gazette).isEqualTo("bgbl-1")
        assertThat(query.year).isEqualTo("2022")
        assertThat(query.page).isEqualTo("1125")
    }
}
