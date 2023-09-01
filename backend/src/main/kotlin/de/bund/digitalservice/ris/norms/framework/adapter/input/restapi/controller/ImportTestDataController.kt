package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.OpenApiConfiguration
import de.bund.digitalservice.ris.norms.application.port.input.ImportTestDataUseCase
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.ApiConfiguration
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeGuid
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.schema.DocumentationRequestSchema
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.schema.FormulaRequestSchema
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.schema.MetadataSectionRequestSchema
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.schema.RecitalsRequestSchema
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(ApiConfiguration.API_NORMS_PATH)
@Tag(name = OpenApiConfiguration.NORMS_TAG)
class ImportTestDataController(private val importTestDataService: ImportTestDataUseCase) {
  @PostMapping(path = ["/test-data"])
  @Operation(
      summary = "Import test data to create a norm",
      description =
          """
          Allows to create examplary norms based on test data of various complexity.
          Used for project internal testing without the regular Juris based input.
          """,
  )
  @ApiResponse(responseCode = "201", description = "Norm was successfully imported")
  fun importTestData(
      @RequestBody testData: RequestSchema,
  ): Mono<ResponseEntity<ResponseSchema>> {
    val norm = testData.toUseCaseData()
    val importCommand = ImportTestDataUseCase.Command(norm)
    return importTestDataService
        .importTestData(importCommand)
        .filter { it }
        .map { ResponseSchema.fromUseCaseData(norm.guid) }
        .map { ResponseEntity.status(201).body(it) }
        .defaultIfEmpty(ResponseEntity.internalServerError().build())
        .onErrorReturn(ResponseEntity.internalServerError().build())
  }

  class RequestSchema {
    var metadataSections: Collection<MetadataSectionRequestSchema> = emptyList()
    var recitals: RecitalsRequestSchema? = null
    var formula: FormulaRequestSchema? = null
    var documentation: Collection<DocumentationRequestSchema> = emptyList()

    fun toUseCaseData() =
        Norm(
            guid = UUID.randomUUID(),
            eGesetzgebung = false,
            metadataSections = metadataSections.map(MetadataSectionRequestSchema::toUseCaseData),
            recitals = recitals?.toUseCaseData(),
            formula = formula?.toUseCaseData(),
            documentation = documentation.map(DocumentationRequestSchema::toUseCaseData),
        )
  }

  data class ResponseSchema private constructor(val guid: String) {
    companion object {
      fun fromUseCaseData(data: UUID) = ResponseSchema(encodeGuid(data))
    }
  }
}
