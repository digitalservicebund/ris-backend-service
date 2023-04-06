package de.bund.digitalservice.ris.norms.application.port.input

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.KEYWORD
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.assertEditNormFrameProperties
import utils.createRandomNormFameProperties
import java.util.UUID

class EditNormFrameCommandTest {

    @Test
    fun `can create command with GUID and new long title`() {
        val guid = UUID.randomUUID()
        val properties = EditNormFrameUseCase.NormFrameProperties("new title", emptyList())
        val command = EditNormFrameUseCase.Command(guid, properties)

        assertThat(command.guid).isEqualTo(guid)
        assertEditNormFrameProperties(command.properties, properties)
    }

    @Test
    fun `can create command with optional fields`() {
        val guid = UUID.randomUUID()
        val metadata = listOf(Metadatum("foo", KEYWORD, 0), Metadatum("bar", KEYWORD, 1))
        val metadataSections = listOf(MetadataSection(MetadataSectionName.GENERAL_INFORMATION, metadata))
        val properties = createRandomNormFameProperties().copy(metadataSections = metadataSections)
        val command = EditNormFrameUseCase.Command(guid, properties)

        assertThat(command.guid).isEqualTo(guid)
        assertEditNormFrameProperties(command.properties, properties)
    }
}
