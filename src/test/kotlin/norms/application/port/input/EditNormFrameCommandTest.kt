package de.bund.digitalservice.ris.norms.application.port.input

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class EditNormFrameCommandTest {
    @Test
    fun `can create command with GUID and new long title`() {
        val guid = UUID.randomUUID()
        val command = EditNormFrameUseCase.Command(guid, "new title")

        assertThat(command.guid).isEqualTo(guid)
        assertThat(command.longTitle).isEqualTo("new title")
    }
}
