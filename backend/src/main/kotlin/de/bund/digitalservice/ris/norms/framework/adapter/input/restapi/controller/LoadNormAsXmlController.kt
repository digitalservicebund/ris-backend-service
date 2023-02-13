package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.norms.application.port.input.LoadNormAsXmlUseCase
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.ApiConfiguration
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

    @GetMapping(
        path = [
            "/norms/xml/eli/{printAnnouncementGazette}/{announcementYear}/s{printAnnouncementPage}",
            "/open/norms/xml/eli/{printAnnouncementGazette}/{announcementYear}/s{printAnnouncementPage}",
        ],
    )
    fun loadNormAsXml(
        @PathVariable printAnnouncementGazette: String,
        @PathVariable announcementYear: String,
        @PathVariable printAnnouncementPage: String,
    ): Mono<ResponseEntity<String>> {
        val query = LoadNormAsXmlUseCase.Query(printAnnouncementGazette, announcementYear, printAnnouncementPage)

        return loadNormAsXmlService
            .loadNormAsXml(query)
            .map { normAsXml ->
                ResponseEntity.ok().contentType(APPLICATION_XML).body(normAsXml)
            }
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .onErrorReturn(ResponseEntity.internalServerError().build())
    }
}
