package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.norms.application.port.input.GetFileUseCase
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.ApiConfiguration
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping(ApiConfiguration.API_NORMS_PATH)
class GetFileController(private val getFileService: GetFileUseCase) {

    @GetMapping(path = ["/{guid}/files/{hash}"], produces = ["application/zip"])
    fun getFile(@PathVariable guid: String, @PathVariable hash: String): Mono<ResponseEntity<ByteArray>> {
        val command = GetFileUseCase.Command(UUID.fromString(guid), hash)
        return getFileService.getFile(command)
            .map { body -> ResponseEntity.ok().body(body) }
            .onErrorReturn(ResponseEntity.internalServerError().build())
    }
}
