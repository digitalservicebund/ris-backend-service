package de.bund.digitalservice.ris.norms.domain.entity

import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContentElementTest {

  @Test
  fun `can create a preamble`() {
    val guid = UUID.randomUUID()
    val preamble = Preamble(guid, 1, text = "Preamble Text")

    assertThat(preamble.guid).isEqualTo(guid)
    assertThat(preamble.order).isEqualTo(1)
    assertThat(preamble.text).isEqualTo("Preamble Text")
  }

  @Test
  fun `can create a paragraph`() {
    val guid = UUID.randomUUID()
    val paragraph = Paragraph(guid, 1, "A", "Paragraph Text")

    assertThat(paragraph.marker).isEqualTo("A")
    assertThat(paragraph.guid).isEqualTo(guid)
    assertThat(paragraph.order).isEqualTo(1)
    assertThat(paragraph.text).isEqualTo("Paragraph Text")
  }

  @Test
  fun `can create a closing`() {
    val guid = UUID.randomUUID()
    val closing = Closing(guid, 2, text = "Closing Text")

    assertThat(closing.guid).isEqualTo(guid)
    assertThat(closing.order).isEqualTo(2)
    assertThat(closing.text).isEqualTo("Closing Text")
  }
}
