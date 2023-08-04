package de.bund.digitalservice.ris.norms.application.port.input

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.KEYWORD
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ValidateNormFrameCommandTest {

  @Test
  fun `can create command with metadata sections list`() {
    val metadata = listOf(Metadatum("foo", KEYWORD, 0), Metadatum("bar", KEYWORD, 1))
    val metadataSections = listOf(MetadataSection(MetadataSectionName.NORM, metadata))
    val command = ValidateNormFrameUseCase.Command(metadataSections)

    assertThat(command.metadataSections).isEqualTo(metadataSections)
  }
}
