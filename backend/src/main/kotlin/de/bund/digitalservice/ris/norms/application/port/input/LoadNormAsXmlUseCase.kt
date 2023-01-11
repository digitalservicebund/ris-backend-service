package de.bund.digitalservice.ris.norms.application.port.input

import reactor.core.publisher.Mono

interface LoadNormAsXmlUseCase {
    fun loadNormAsXml(query: Query): Mono<String>

    data class Query(
        val printAnnouncementGazette: String,
        val announcementOrCitationYear: String,
        val printAnnouncementPage: String
    )
}
