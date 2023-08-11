package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.OpenApiConfiguration
import de.bund.digitalservice.ris.norms.application.port.input.MigrateNormUseCase
import de.bund.digitalservice.ris.norms.application.port.input.MigrateNormUseCase.ConverterNorms
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.ApiConfiguration
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
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
class MigrateNormController(private val migrateNormService: MigrateNormUseCase) {

  @PostMapping(path = ["/migrate"])
  @Operation(
      summary = "Sends a batch of norms to the application",
      description =
          "Saves a batch of norms exported from the juris-norm-converter application and place them in the ris-backend application")
  @ApiResponses(
      ApiResponse(responseCode = "201"),
      ApiResponse(responseCode = "401"),
  )
  fun createNorm(
      @SwaggerRequestBody(
          description = "Norms to be migrated",
          required = true,
          content = [Content(schema = Schema(implementation = ConverterNorms::class))])
      @RequestBody
      request: ConverterNorms
  ): Mono<ResponseEntity<Boolean>> {
    val command = MigrateNormUseCase.Command(request)

    return migrateNormService
        .migrateNorm(command)
        .flatMap { body -> Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(body)) }
        .onErrorReturn(ResponseEntity.internalServerError().build())
  }
}
