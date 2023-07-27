package de.bund.digitalservice.ris.norms.application.port.input

import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LoadNormQueryTest {
  @Test
  fun `can create query with GUID`() {
    val guid = UUID.randomUUID()
    val query = LoadNormUseCase.Query(guid)

    assertThat(query.guid).isEqualTo(guid)
  }
}
