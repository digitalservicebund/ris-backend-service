package de.bund.digitalservice.ris.norms.application.port.output

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.util.UUID

class ParseJurisXmlQueryTest {

    @Test
    fun `can create query with a new GUID and a Zip file`() {
        val newGuid = UUID.randomUUID()
        val zipFile = File.createTempFile("Temp", ".zip")
        val query = ParseJurisXmlOutputPort.Query(newGuid, zipFile)

        assertThat(query.newGuid).isEqualTo(newGuid)
        assertThat(query.zipFile).isEqualTo(zipFile)
    }
}
