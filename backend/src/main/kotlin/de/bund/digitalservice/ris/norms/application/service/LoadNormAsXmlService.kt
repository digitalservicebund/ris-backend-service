package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.LoadNormAsXmlUseCase
import de.bund.digitalservice.ris.norms.application.port.output.ConvertNormToXmlOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByEliOutputPort
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class LoadNormAsXmlService(
    private val getNormByEliAdapter: GetNormByEliOutputPort,
    private val convertNormToXmlAdapter: ConvertNormToXmlOutputPort,
) : LoadNormAsXmlUseCase {

    override fun loadNormAsXml(query: LoadNormAsXmlUseCase.Query): Mono<String> {
        val eliQuery =
            GetNormByEliOutputPort.Query(
                query.printAnnouncementGazette,
                query.announcementOrCitationYear,
                query.printAnnouncementPage,
            )

        return getNormByEliAdapter
            .getNormByEli(eliQuery)
            .map { norm -> ConvertNormToXmlOutputPort.Command(norm) }
            .flatMap { command -> convertNormToXmlAdapter.convertNormToXml(command) }
    }
}
