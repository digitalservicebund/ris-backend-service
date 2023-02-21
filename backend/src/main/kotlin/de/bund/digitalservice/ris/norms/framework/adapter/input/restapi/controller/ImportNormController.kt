package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.ApiConfiguration
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeGuid
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping(ApiConfiguration.API_NORMS_PATH)
class ImportNormController(private val importNormService: ImportNormUseCase) {

    @PostMapping
    fun createNorm(@RequestBody zipFile: ByteArray, @RequestHeader headers: HttpHeaders): Mono<ResponseEntity<ResponseSchema>> {
        val filename = headers.getFirst("X-Filename") ?: "norm.zip"
        val command = ImportNormUseCase.Command(zipFile, filename)

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
