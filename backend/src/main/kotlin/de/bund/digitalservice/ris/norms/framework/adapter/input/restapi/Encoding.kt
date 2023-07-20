package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import de.bund.digitalservice.ris.norms.domain.value.Eli
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

fun decodeGuid(guidString: String): UUID = UUID.fromString(guidString)

fun encodeGuid(guid: UUID): String = guid.toString()

fun decodeLocalDate(dateString: String?): LocalDate? =
    if (dateString != null) LocalDate.parse(dateString) else null

fun decodeLocalTime(timeString: String?): LocalTime? =
    if (timeString != null) LocalTime.parse(timeString) else null

fun encodeLocalDate(date: LocalDate?): String? = date?.toString()

fun encodeLocalDateToGermanFormat(date: LocalDate?): String? = date?.format(DateTimeFormatter.ofPattern("d.M.yyyy"))

fun encodeLocalDateTime(dateTime: LocalDateTime): String = dateTime.toString()

fun encodeEli(identifier: Eli) = identifier.toString()
