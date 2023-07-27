package de.bund.digitalservice.ris.norms.application.port.output

import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GetNormByGuidQueryTest {

  @Test
  fun `can create query with all required fields`() {
    val guid = UUID.randomUUID()
    val query = GetNormByGuidOutputPort.Query(guid)

    assertThat(query.guid).isEqualTo(guid)
  }
}
