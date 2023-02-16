package de.bund.digitalservice.ris.norms.application.port.input

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer

class ImportNormCommandTest {

    @Test
    fun `can create command with ZIP file`() {
        val zipFile = ByteBuffer.allocate(0)
        val command = ImportNormUseCase.Command(zipFile)

        assertThat(command.zipFile).isEqualTo(zipFile)
    }
}
