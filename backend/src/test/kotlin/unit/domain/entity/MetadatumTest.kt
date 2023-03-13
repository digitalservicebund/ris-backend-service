package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.entity.MetadatumType.KEYWORD
import org.assertj.core.api.Assertions.assertThat
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

        assertThat(datum.order).isEqualTo(0)
    }
}
