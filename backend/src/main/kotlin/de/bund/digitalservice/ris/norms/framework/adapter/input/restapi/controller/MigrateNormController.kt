package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.norms.application.port.input.MigrateNormUseCase
import de.bund.digitalservice.ris.norms.application.port.input.MigrateNormUseCase.ConverterNorms
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.ApiConfiguration
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.OpenApiConfiguration
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
  fun createNorm(@RequestBody request: ConverterNorms): Mono<ResponseEntity<Boolean>> {
    val command = MigrateNormUseCase.Command(request)

    return migrateNormService
        .migrateNorm(command)
        .flatMap { body -> Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(body)) }
        .onErrorReturn(ResponseEntity.internalServerError().build())
  }
}
