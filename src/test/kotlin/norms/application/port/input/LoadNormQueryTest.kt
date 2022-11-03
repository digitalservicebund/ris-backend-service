package de.bund.digitalservice.ris.norms.application.port.input

import de.bund.digitalservice.ris.norms.domain.value.Guid
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LoadNormQueryTest {
    @Test
    fun `can create query with GUID`() {
        val guid = Guid.generateNew()
        val query = LoadNormUseCase.Query(guid)

        assertThat(query.guid).isEqualTo(guid)
    }
}
