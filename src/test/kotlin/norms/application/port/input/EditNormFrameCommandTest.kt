package de.bund.digitalservice.ris.norms.application.port.input

import norms.utils.assertEditNormFrameProperties
import norms.utils.createRandomNormFameProperties
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class EditNormFrameCommandTest {

    @Test
    fun `can create command with GUID and new long title`() {
        val guid = UUID.randomUUID()
        val properties = EditNormFrameUseCase.NormFrameProperties("new title")
        val command = EditNormFrameUseCase.Command(guid, properties)

        assertTrue(command.guid == guid)
        assertEditNormFrameProperties(command.properties, properties)
    }

    @Test
    fun `can create command with optional fields`() {
        val normFrameProperties: EditNormFrameUseCase.NormFrameProperties = createRandomNormFameProperties()

        val guid = UUID.randomUUID()
        val command = EditNormFrameUseCase.Command(guid, normFrameProperties)

        assertTrue(command.guid == guid)
        assertEditNormFrameProperties(command.properties, normFrameProperties)
    }
}
