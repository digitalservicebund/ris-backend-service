package de.bund.digitalservice.ris.norms.application.port.input

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class LoadNormQueryTest {
    @Test
    fun `can create query with GUID`() {
        val guid = UUID.randomUUID()
        val query = LoadNormUseCase.Query(guid)

        assertThat(query.guid).isEqualTo(guid)
    }
}
