package de.bund.digitalservice.ris.norms.application.port.input

import norms.utils.assertEditNormFrameProperties
import norms.utils.createRandomNormFameProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class EditNormFrameCommandTest {

    @Test
    fun `can create command with GUID and new long title`() {
        val guid = UUID.randomUUID()
        val properties = EditNormFrameUseCase.NormFrameProperties("new title")
        val command = EditNormFrameUseCase.Command(guid, properties)

        assertThat(command.guid).isEqualTo(guid)
        assertEditNormFrameProperties(command.properties, properties)
    }

    @Test
    fun `can create command with optional fields`() {
        val normFrameProperties = createRandomNormFameProperties()

        val guid = UUID.randomUUID()
        val command = EditNormFrameUseCase.Command(guid, normFrameProperties)

        assertThat(command.guid).isEqualTo(guid)
        assertEditNormFrameProperties(command.properties, normFrameProperties)
    }
}
