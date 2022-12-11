package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import ApiConfiguration
import de.bund.digitalservice.ris.norms.application.port.input.LoadNormAsXmlUseCase
import decodeGuid
import org.springframework.http.MediaType.APPLICATION_XML
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(ApiConfiguration.API_BASE_PATH)
class LoadNormAsXmlController(private val loadNormAsXmlService: LoadNormAsXmlUseCase) {

    @GetMapping(path = ["/xml/{guid}"])
    fun loadNormAsXml(@PathVariable guid: String): Mono<ResponseEntity<String>> {
        val query = LoadNormAsXmlUseCase.Query(decodeGuid(guid))

        return loadNormAsXmlService
            .loadNormAsXml(query)
            .map({ normAsXml -> ResponseEntity.ok().contentType(APPLICATION_XML).body(normAsXml) })
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .onErrorReturn(ResponseEntity.internalServerError().build())
    }
}
