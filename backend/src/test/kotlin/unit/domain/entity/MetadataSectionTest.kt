package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_LONG_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_SHORT_TITLE
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchException
import org.junit.jupiter.api.Test

class MetadataSectionTest {
    @Test
    fun `can create metadataSection for headings and abbreviations`() {
        val unofficialLongTitle = Metadatum("unofficialLongTitle", UNOFFICIAL_LONG_TITLE)
        val unofficialShortTitle = Metadatum("unofficialShortTitle", UNOFFICIAL_SHORT_TITLE)
        val section = MetadataSection(
            MetadataSectionName.HEADINGS_AND_ABBREVIATIONS,
            listOf(unofficialLongTitle, unofficialShortTitle),
        )

        assertThat(section.name).isEqualTo(MetadataSectionName.HEADINGS_AND_ABBREVIATIONS)
        assertThat(section.metadata).isEqualTo(listOf(unofficialLongTitle, unofficialShortTitle))
        assertThat(section.sections).isNull()
    }

    @Test
    fun `it throws an illegal argument exception if child sections are incorrect`() {
        val unofficialLongTitle = Metadatum("unofficialLongTitle", UNOFFICIAL_LONG_TITLE)
        val unofficialShortTitle = Metadatum("unofficialShortTitle", UNOFFICIAL_SHORT_TITLE)
        val section = MetadataSection(
            MetadataSectionName.HEADINGS_AND_ABBREVIATIONS,
            listOf(unofficialLongTitle, unofficialShortTitle),
        )
        val exception = catchException {
            MetadataSection(
                MetadataSectionName.HEADINGS_AND_ABBREVIATIONS,
                listOf(unofficialLongTitle, unofficialShortTitle),
                listOf(section),
            )
        }

        assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(exception.message)
            .isEqualTo("Incorrect children for section '${section.name}'")
    }

    @Test
    fun `it throws an illegal argument exception if metadata do not belong`() {
        val unofficialLongTitle = Metadatum("unofficialLongTitle", UNOFFICIAL_LONG_TITLE)
        val unofficialReference = Metadatum("unofficialReference", UNOFFICIAL_REFERENCE)
        val exception = catchException {
            MetadataSection(
                MetadataSectionName.HEADINGS_AND_ABBREVIATIONS,
                listOf(unofficialLongTitle, unofficialReference),
            )
        }

        assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(exception.message)
            .isEqualTo("Incorrect metadata for section '${MetadataSectionName.HEADINGS_AND_ABBREVIATIONS}'")
    }
}
