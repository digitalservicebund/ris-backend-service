package norms.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import encodeLocalDate
import java.lang.reflect.Type
import java.time.LocalDate

fun convertEditNormFramePropertiesToJson(normFrameProperties: EditNormFrameUseCase.NormFrameProperties): String {
    class LocalDateSerializer : JsonSerializer<LocalDate> {
        override fun serialize(src: LocalDate?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return JsonPrimitive(encodeLocalDate(src))
        }
    }
    val gson: Gson = GsonBuilder().registerTypeAdapter(
        LocalDate::class.java,
        LocalDateSerializer()
    ).create()
    return gson.toJson(normFrameProperties)
}
