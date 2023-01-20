package de.bund.digitalservice.ris.norms.application.port.output

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class GetNormByGuidQueryTest {

    @Test
    fun `can create query with all required fields`() {
        val guid = UUID.randomUUID()
        val query = GetNormByGuidOutputPort.Query(guid)

        assertThat(query.guid).isEqualTo(guid)
    }
}
