package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.norms.application.port.input.GetFileUseCase
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.ApiConfiguration
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.OpenApiConfiguration
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(ApiConfiguration.API_NORMS_PATH)
@Tag(name = OpenApiConfiguration.NORMS_TAG)
class GetFileController(private val getFileService: GetFileUseCase) {

  @GetMapping(path = ["/{guid}/files/{hash}"], produces = ["application/zip"])
  @Operation(
      summary = "Download the exported ZIP file of a norm",
      description = "Downloads a norm in a compressed ZIP file containing all juris xml files")
  @ApiResponses(
      ApiResponse(responseCode = "200", description = "Download ZIP file"),
      ApiResponse(
          responseCode = "404",
          description = "Norm with the given guid or given hash file name not found"),
  )
  fun getFile(
      @PathVariable guid: String,
      @PathVariable hash: String
  ): Mono<ResponseEntity<ByteArray>> {
    val command = GetFileUseCase.Command(UUID.fromString(guid), hash)
    return getFileService
        .getFile(command)
        .map { body -> ResponseEntity.ok().body(body) }
        .onErrorReturn(ResponseEntity.internalServerError().build())
  }
}
