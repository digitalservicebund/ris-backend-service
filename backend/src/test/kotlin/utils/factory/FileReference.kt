package utils.factory

import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import utils.randomString
import java.time.LocalDateTime

fun file(block: FileBuilder.() -> Unit): FileReference = FileBuilder().apply(block).build()

class FileBuilder {
    var name: String = randomString(10) + ".zip"
    var hash: String = randomString(32)
    var createdAt: LocalDateTime = LocalDateTime.now()

    fun build(): FileReference = FileReference(name, hash, createdAt)
}
