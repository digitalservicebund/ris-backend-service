import java.time.LocalDate
import java.util.UUID

fun decodeGuid(guidString: String): UUID = UUID.fromString(guidString)

fun encodeGuid(guid: UUID): String = guid.toString()

fun decodeLocalDate(dateString: String?): LocalDate? =
    if (dateString != null) LocalDate.parse(dateString) else null

fun encodeLocalDate(date: LocalDate?): String? = if (date != null) date.toString() else null
