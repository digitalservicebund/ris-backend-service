package de.bund.digitalservice.ris.norms.application.port.input

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class ImportNormCommandTest {

    @Test
    fun `can create command with ZIP file`() {
        val zipFile = File.createTempFile("Temp", ".zip")
        val command = ImportNormUseCase.Command(zipFile.readBytes(), zipFile.name, zipFile.length())

        assertThat(command.zipFile).isEqualTo(zipFile.readBytes())
    }
}
