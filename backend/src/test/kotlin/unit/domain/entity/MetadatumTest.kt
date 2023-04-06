package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.KEYWORD
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchException
import org.junit.jupiter.api.Test

class MetadatumTest {
    @Test
    fun `can create metadatum for a keyword`() {
        val datum = Metadatum("test word", KEYWORD, 5)

        assertThat(datum.value).isEqualTo("test word")
        assertThat(datum.type).isEqualTo(KEYWORD)
        assertThat(datum.order).isEqualTo(5)
    }

    @Test
    fun `it has per default the order number zero`() {
        val datum = Metadatum("test word", KEYWORD)

        assertThat(datum.order).isEqualTo(1)
    }

    @Test
    fun `it throws an illegal argument exception of value type is not correct`() {
        val exception = catchException { Metadatum(1, KEYWORD) }

        assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(exception.message)
            .isEqualTo("Incorrect value type 'Integer' for datum type 'KEYWORD'")
    }
}
