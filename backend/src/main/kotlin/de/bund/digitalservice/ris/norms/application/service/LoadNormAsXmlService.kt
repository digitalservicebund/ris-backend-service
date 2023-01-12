package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.LoadNormAsXmlUseCase
import de.bund.digitalservice.ris.norms.application.port.output.ConvertNormToXmlOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByEliOutputPort
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class LoadNormAsXmlService(
    private val getNormByEliAdapter: GetNormByEliOutputPort,
    private val convertNormToXmlAdapter: ConvertNormToXmlOutputPort
) : LoadNormAsXmlUseCase {

    override fun loadNormAsXml(query: LoadNormAsXmlUseCase.Query): Mono<String> {
        return getNormByEliAdapter.getNormByEli(
            query.printAnnouncementGazette,
            query.announcementOrCitationYear,
            query.printAnnouncementPage
        ).flatMap { norm ->
            convertNormToXmlAdapter.convertNormToXml(norm)
        }
    }
}
