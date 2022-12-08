package norms.framework.adapter.input.restapi

import decodeLocalDate
import encodeLocalDate
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class EncodingTest {

    @Test
    fun `can decode a date from string to localdate`() {
        val dateString = "2020-10-21"
        val date = decodeLocalDate(dateString)

        assertTrue(date == LocalDate.of(2020, 10, 21))
    }

    @Test
    fun `can decode an empty date from string to null`() {
        val date = decodeLocalDate(null)

        assertTrue(date == null)
    }

    @Test
    fun `can encode a date from localdate to string`() {
        val date = LocalDate.of(2020, 10, 21)
        val dateString = encodeLocalDate(date)

        assertTrue(dateString == "2020-10-21")
    }

    @Test
    fun `can encode an empty date from localdate to null`() {
        val date = encodeLocalDate(null)

        assertTrue(date == null)
    }
}
