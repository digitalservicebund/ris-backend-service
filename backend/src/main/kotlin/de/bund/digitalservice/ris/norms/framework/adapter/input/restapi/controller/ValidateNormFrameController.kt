package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.norms.application.port.input.ValidateNormFrameUseCase
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.specification.SpecificationViolation
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.ApiConfiguration
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.OpenApiConfiguration
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.schema.MetadataSectionRequestSchema
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(ApiConfiguration.API_NORMS_PATH)
@Tag(name = OpenApiConfiguration.NORMS_TAG)
class ValidateNormFrameController(private val validateNormFrameUseCase: ValidateNormFrameUseCase) {

  @PostMapping(path = ["/norm/validation"])
  @Operation(
      summary = "Validate a norm",
      description =
          "Parse the norm frame data against the specification to check if the values are valid")
  @ApiResponses(
      ApiResponse(responseCode = "204", description = "Norm was updated"),
      ApiResponse(responseCode = "400"),
  )
  fun validateNormFrame(
      @RequestBody request: NormFramePropertiesRequestSchema,
  ): Mono<ResponseEntity<List<SpecificationViolation>>> {
    val metadataSections = request.toUseCaseData()
    val command = ValidateNormFrameUseCase.Command(metadataSections)

    return validateNormFrameUseCase.validateNormFrame(command).map {
      ResponseEntity.status(HttpStatus.OK).body(it.violations)
    }
  }

  class NormFramePropertiesRequestSchema {
    lateinit var metadataSections: List<MetadataSectionRequestSchema>

    fun toUseCaseData(): List<MetadataSection> = this.metadataSections.map { it.toUseCaseData() }
  }
}
