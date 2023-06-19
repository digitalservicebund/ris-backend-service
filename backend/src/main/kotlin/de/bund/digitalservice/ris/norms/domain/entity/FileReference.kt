package de.bund.digitalservice.ris.norms.domain.entity

import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.*

data class FileReference(
    val name: String,
    val hash: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val guid: UUID,
)

fun getHashFromContent(bytes: ByteArray): String = HexFormat
    .of().formatHex(
        MessageDigest
            .getInstance("SHA-256")
            .digest(bytes),
    )
