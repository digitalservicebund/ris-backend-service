package de.bund.digitalservice.ris.norms.domain.entity

import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.HexFormat

data class FileReference(
    val name: String,
    val hash: String,
    val createdAt: LocalDateTime,
)

fun getHashFromContent(bytes: ByteArray): String = HexFormat
    .of().formatHex(
        MessageDigest
            .getInstance("SHA-256")
            .digest(bytes),
    )
