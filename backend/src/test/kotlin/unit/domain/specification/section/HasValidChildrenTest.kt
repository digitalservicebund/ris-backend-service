package de.bund.digitalservice.ris.norms.domain.specification.section

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class HasValidChildrenTest {
    @Test
    fun `it is satisfied if the children of the section are null`() {
        val instance = mockk<MetadataSection>()
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }
}
