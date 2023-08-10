package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.OpenApiConfiguration
import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.ApiConfiguration
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.decodeGuid
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.schema.MetadataSectionRequestSchema
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(ApiConfiguration.API_NORMS_PATH)
@Tag(name = OpenApiConfiguration.NORMS_TAG)
class EditNormFrameController(private val editNormFrameService: EditNormFrameUseCase) {

  @PutMapping(path = ["/{guid}"])
  @Operation(
      summary = "Edit the frame data of a norm",
      description = "Edits a norm given its unique guid identifier")
  @ApiResponses(
      ApiResponse(responseCode = "204", description = "Norm was updated"),
      ApiResponse(responseCode = "400"),
  )
  fun editNormFrame(
      @Parameter(
          name = "guid", description = "the unique guid identifier of a norm", required = true)
      @PathVariable
      guid: String,
      @RequestBody request: NormFramePropertiesRequestSchema,
  ): Mono<ResponseEntity<Unit>> {
    val properties = request.toUseCaseData()
    val command = EditNormFrameUseCase.Command(decodeGuid(guid), properties)

    return editNormFrameService.editNormFrame(command).map {
      ResponseEntity.noContent().build<Unit>()
    }
  }

  class NormFramePropertiesRequestSchema {
    lateinit var metadataSections: List<MetadataSectionRequestSchema>

    var eli: String? = null

    fun toUseCaseData(): EditNormFrameUseCase.NormFrameProperties =
        EditNormFrameUseCase.NormFrameProperties(
            this.metadataSections.map { it.toUseCaseData() },
        )
  }
}
