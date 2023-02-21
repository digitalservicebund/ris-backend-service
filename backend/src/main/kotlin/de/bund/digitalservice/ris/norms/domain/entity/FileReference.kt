package de.bund.digitalservice.ris.norms.domain.entity

import java.security.MessageDigest
import java.util.HexFormat
import java.io.File as JavaFile

data class FileReference(
    val name: String,
    val hash: String,
) {
    companion object {
        fun fromFile(file: JavaFile) = FileReference(file.name, getHashFromContent(file.readBytes()))
    }
}

private fun getHashFromContent(bytes: ByteArray): String = HexFormat
    .of().formatHex(
        MessageDigest
            .getInstance("MD5")
            .digest(bytes),
    )
