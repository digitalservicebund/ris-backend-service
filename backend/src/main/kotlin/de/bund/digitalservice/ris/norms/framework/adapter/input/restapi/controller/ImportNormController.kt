package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.ApiConfiguration
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.OpenApiConfiguration
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeGuid
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(ApiConfiguration.API_NORMS_PATH)
@Tag(name = OpenApiConfiguration.NORMS_TAG)
class ImportNormController(private val importNormService: ImportNormUseCase) {

  @PostMapping
  @Operation(
      summary = "Import a norm using a ZIP",
      description = "Importing all xml files of a norm in juris format in a compressed ZIP file")
  @ApiResponse(responseCode = "201", description = "Norm was successfully imported")
  fun createNorm(
      @RequestBody zipFile: ByteArray,
      @RequestHeader headers: HttpHeaders
  ): Mono<ResponseEntity<ResponseSchema>> {
    val filename = headers.getFirst("X-Filename") ?: "norm.zip"
    val command = ImportNormUseCase.Command(zipFile, filename, headers.contentLength)

    return importNormService
        .importNorm(command)
        .map { data -> ResponseSchema.fromUseCaseData(data) }
        .map { body -> ResponseEntity.status(201).body(body) }
        .onErrorReturn(ResponseEntity.internalServerError().build())
  }

  data class ResponseSchema private constructor(val guid: String) {
    companion object {
      fun fromUseCaseData(data: UUID) = ResponseSchema(encodeGuid(data))
    }
  }
}
