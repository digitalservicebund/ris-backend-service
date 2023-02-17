package de.bund.digitalservice.ris.norms.application.port.output

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.util.UUID

class ParseJurisXmlQueryTest {

    @Test
    fun `can create query with a new GUID and a Zip file`() {
        val newGuid = UUID.randomUUID()
        val zipFile = ByteBuffer.allocate(0)
        val query = ParseJurisXmlOutputPort.Query(newGuid, zipFile)

        assertThat(query.newGuid).isEqualTo(newGuid)
        assertThat(query.zipFile).isEqualTo(zipFile)
    }
}
