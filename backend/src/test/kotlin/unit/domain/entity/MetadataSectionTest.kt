package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_LONG_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_SHORT_TITLE
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchException
import org.junit.jupiter.api.Test

class MetadataSectionTest {
    private val unofficialLongTitle = Metadatum("unofficialLongTitle", UNOFFICIAL_LONG_TITLE)
    private val unofficialShortTitle = Metadatum("unofficialShortTitle", UNOFFICIAL_SHORT_TITLE)

    @Test
    fun `can create metadataSection for headings and abbreviations`() {
        val section = MetadataSection(
            MetadataSectionName.NORM,
            listOf(unofficialLongTitle, unofficialShortTitle),
        )

        assertThat(section.name).isEqualTo(MetadataSectionName.NORM)
        assertThat(section.metadata).isEqualTo(listOf(unofficialLongTitle, unofficialShortTitle))
        assertThat(section.sections).isNull()
        assertThat(section.order).isEqualTo(1)
    }

    @Test
    fun `can create metadataSection with different order`() {
        val section = MetadataSection(
            MetadataSectionName.NORM,
            listOf(unofficialLongTitle, unofficialShortTitle),
            5,
        )

        assertThat(section.order).isEqualTo(5)
    }

    @Test
    fun `it throws an illegal argument exception if child sections are incorrect`() {
        val section = MetadataSection(
            MetadataSectionName.NORM,
            listOf(unofficialLongTitle, unofficialShortTitle),
        )
        val exception = catchException {
            MetadataSection(
                MetadataSectionName.NORM,
                listOf(unofficialLongTitle, unofficialShortTitle),
                1,
                listOf(section),
            )
        }

        assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(exception.message)
            .isEqualTo("Incorrect children for section '${section.name}'")
    }

    @Test
    fun `it throws an illegal argument exception if metadata do not belong`() {
        val unofficialReference = Metadatum("unofficialReference", UNOFFICIAL_REFERENCE)
        val exception = catchException {
            MetadataSection(
                MetadataSectionName.NORM,
                listOf(unofficialLongTitle, unofficialReference),
            )
        }

        assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(exception.message)
            .isEqualTo("Incorrect metadata for section '${MetadataSectionName.NORM}'")
    }
}
