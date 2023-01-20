package de.bund.digitalservice.ris.norms.application.port.output

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.createRandomNorm

class SaveNormCommandTest {

    @Test
    fun `can create command with a norm`() {
        val norm = createRandomNorm()
        val command = SaveNormOutputPort.Command(norm)

        assertThat(command.norm).isEqualTo(norm)
    }
}
