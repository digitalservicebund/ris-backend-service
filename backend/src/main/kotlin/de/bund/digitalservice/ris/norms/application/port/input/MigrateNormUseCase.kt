package de.bund.digitalservice.ris.norms.application.port.input

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.bund.digitalservice.ris.norms.juris.converter.model.Norm
import java.util.*
import reactor.core.publisher.Mono

fun interface MigrateNormUseCase {
  fun migrateNorm(command: Command): Mono<Boolean>

  data class Command(val norms: ConverterNorms)

  @JsonDeserialize(using = ConverterNormsDeserializer::class)
  data class ConverterNorms(val norms: List<ConverterNorm>)

  data class ConverterNorm(val guid: UUID, val norm: Norm)

  class ConverterNormsDeserializer : JsonDeserializer<ConverterNorms>() {
    override fun deserialize(
        parser: JsonParser?,
        context: DeserializationContext?
    ): ConverterNorms {
      val kotlinModule: KotlinModule =
          KotlinModule.Builder().configure(KotlinFeature.StrictNullChecks, true).build()
      val mapper = ObjectMapper().registerModule(kotlinModule)
      val schema: TreeNode = mapper.readTree(parser)
      val norms =
          mapper
              .readerForListOf(ConverterNorm::class.java)
              .readValue<List<ConverterNorm>>(schema["norms"].toString())
      return ConverterNorms(norms)
    }
  }
}
