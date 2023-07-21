package de.bund.digitalservice.ris.norms.domain.specification.metadatum

import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.APPENDIX
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.ARTICLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DATE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DESCRIPTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.ENTRY_INTO_FORCE_DATE_NOTE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.EXTERNAL_DATA_NOTE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.FOOTNOTE_CHANGE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.FOOTNOTE_COMMENT
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.FOOTNOTE_DECISION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.FOOTNOTE_EU_LAW
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.FOOTNOTE_OTHER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.FOOTNOTE_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.FOOTNOTE_STATE_LAW
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.KEYWORD
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.LINK
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.NORM_CATEGORY
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.NOTE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PROOF_INDICATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RANGE_END
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RANGE_START
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RELATED_DATA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RESOLUTION_MAJORITY
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.TEXT
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNDEFINED_DATE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.WORK_NOTE
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasValidValueTypeTest {
    @Test
    fun `it is satisfied if the value for a keyword is a string`() {
        val instance = getMockedMetadatum("foo", KEYWORD)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for a keyword is not a string`() {
        val instance = getMockedMetadatum(103, KEYWORD)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is not satisfied if the value for a date is a string`() {
        val instance = getMockedMetadatum("citation date", DATE)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is not satisfied if the value for a range start is not a string`() {
        val instance = getMockedMetadatum(123, RANGE_START)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is not satisfied if the value for a range end is not a string`() {
        val instance = getMockedMetadatum(123, RANGE_END)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is not satisfied if the value for a resolution majority is a string`() {
        val instance = getMockedMetadatum("resolution majority", RESOLUTION_MAJORITY)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is not satisfied if the value for a norm category is a string`() {
        val instance = getMockedMetadatum("norm category", NORM_CATEGORY)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is not satisfied if the value for an undefined date is a string`() {
        val instance = getMockedMetadatum("undefined date", UNDEFINED_DATE)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied if the value for a text is a string`() {
        val instance = getMockedMetadatum("test text", TEXT)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for a text is not string`() {
        val instance = getMockedMetadatum(123, TEXT)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied if the value for a link is a string`() {
        val instance = getMockedMetadatum("test link", LINK)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for a link is not string`() {
        val instance = getMockedMetadatum(123, LINK)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied if the value for related data is a string`() {
        val instance = getMockedMetadatum("test related data", RELATED_DATA)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for related data is not string`() {
        val instance = getMockedMetadatum(123, RELATED_DATA)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied if the value for an external data note is a string`() {
        val instance = getMockedMetadatum("test external data note", EXTERNAL_DATA_NOTE)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for an external data note is not string`() {
        val instance = getMockedMetadatum(123, EXTERNAL_DATA_NOTE)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied if the value for appendix is a string`() {
        val instance = getMockedMetadatum("test appendix", APPENDIX)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for an appendix is not string`() {
        val instance = getMockedMetadatum(123, APPENDIX)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied if the value for a footnote reference is a string`() {
        val instance = getMockedMetadatum("test text", FOOTNOTE_REFERENCE)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for a footnote reference is not string`() {
        val instance = getMockedMetadatum(123, FOOTNOTE_REFERENCE)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied if the value for a footnote change is a string`() {
        val instance = getMockedMetadatum("test text", FOOTNOTE_CHANGE)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for a footnote change is not string`() {
        val instance = getMockedMetadatum(123, FOOTNOTE_CHANGE)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied if the value for a footnote comment is a string`() {
        val instance = getMockedMetadatum("test text", FOOTNOTE_COMMENT)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for a footnote comment is not string`() {
        val instance = getMockedMetadatum(123, FOOTNOTE_COMMENT)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied if the value for a footnote decision is a string`() {
        val instance = getMockedMetadatum("test text", FOOTNOTE_DECISION)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for a footnote decision is not string`() {
        val instance = getMockedMetadatum(123, FOOTNOTE_DECISION)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied if the value for a footnote state law is a string`() {
        val instance = getMockedMetadatum("test text", FOOTNOTE_STATE_LAW)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for a footnote state law is not string`() {
        val instance = getMockedMetadatum(123, FOOTNOTE_STATE_LAW)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied if the value for a footnote eu law is a string`() {
        val instance = getMockedMetadatum("test text", FOOTNOTE_EU_LAW)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for a footnote eu law is not string`() {
        val instance = getMockedMetadatum(123, FOOTNOTE_EU_LAW)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied if the value for a footnote other is a string`() {
        val instance = getMockedMetadatum("test text", FOOTNOTE_OTHER)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for a footnote other is not string`() {
        val instance = getMockedMetadatum(123, FOOTNOTE_OTHER)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied if the value for a work note is a string`() {
        val instance = getMockedMetadatum("test text", WORK_NOTE)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for a work note is not string`() {
        val instance = getMockedMetadatum(123, WORK_NOTE)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied if the value for a description is a string`() {
        val instance = getMockedMetadatum("test text", DESCRIPTION)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for a description is not string`() {
        val instance = getMockedMetadatum(123, DESCRIPTION)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied if the value for a reference is a string`() {
        val instance = getMockedMetadatum("test text", REFERENCE)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for a reference is not string`() {
        val instance = getMockedMetadatum(123, REFERENCE)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied if the value for an entry into force date note is a string`() {
        val instance = getMockedMetadatum("test text", ENTRY_INTO_FORCE_DATE_NOTE)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for an entry into force date note  is not string`() {
        val instance = getMockedMetadatum(123, ENTRY_INTO_FORCE_DATE_NOTE)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is not satisfied if the value for a proof indication type is not a string`() {
        val instance = getMockedMetadatum(123, PROOF_INDICATION)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied if the value for note type is a string`() {
        val instance = getMockedMetadatum("test text", NOTE)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for a note is not string`() {
        val instance = getMockedMetadatum(123, NOTE)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied if the value for article type is a string`() {
        val instance = getMockedMetadatum("test text", ARTICLE)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for a article is not string`() {
        val instance = getMockedMetadatum(123, ARTICLE)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }
}

/**
 * Creates a mocked instance of a [Metadatum]. This is necessary because the [Metadatum] class is
 * using this specification here itself within the initialization block. Thereby it is never
 * possible to create an instance that is not satisfying this specification. To still being able to
 * independently and fully test this specification, we use a mocked version of the instance type.
 */
private fun <T : Any> getMockedMetadatum(value: T, type: MetadatumType): Metadatum<T> {
    val instance = mockk<Metadatum<T>>()
    every { instance.value } returns value
    every { instance.type } returns type
    return instance
}
