package de.bund.digitalservice.ris.norms.domain.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.*

class FileReferenceTest {
    @Test
    fun `can create a hash out of file content using sha-265`() {
        val bytes = "TestText".toByteArray()
        val hash = HexFormat.of().formatHex(
            MessageDigest
                .getInstance("SHA-256")
                .digest(bytes),
        )
        val name = "test.zip"
        val time = LocalDateTime.now()

        val fileReference = FileReference(name, getHashFromContent(bytes), time, UUID.randomUUID())
        assertThat(fileReference.name).isEqualTo(name)
        assertThat(fileReference.hash).isEqualTo(hash)
        assertThat(fileReference.createdAt).isEqualTo(time)
    }
}
