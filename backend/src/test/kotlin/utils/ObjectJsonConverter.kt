package utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller.EditNormFrameControllerTest
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeLocalDateTime
import java.lang.reflect.Type
import java.time.LocalDateTime

fun convertEditNormRequestTestSchemaToJson(
    editNormRequestSchema: EditNormFrameControllerTest.NormFramePropertiesTestRequestSchema,
): String {
  return Gson().toJson(editNormRequestSchema)
}

fun convertFileReferenceToJson(fileReference: FileReference): String {
  class LocalDateTimeSerializer : JsonSerializer<LocalDateTime> {
    override fun serialize(
        src: LocalDateTime,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ): JsonElement {
      return JsonPrimitive(encodeLocalDateTime(src))
    }
  }
  val gson: Gson =
      GsonBuilder()
          .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
          .create()
  return gson.toJson(fileReference)
}
