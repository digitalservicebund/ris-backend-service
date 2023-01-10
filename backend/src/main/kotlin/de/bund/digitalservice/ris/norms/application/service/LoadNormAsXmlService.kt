package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.LoadNormAsXmlUseCase
import de.bund.digitalservice.ris.norms.application.port.output.ConvertNormToXmlOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort.QueryParameter
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class LoadNormAsXmlService(
    private val searchNormsAdapter: SearchNormsOutputPort,
    private val convertNormToXmlAdapter: ConvertNormToXmlOutputPort
) : LoadNormAsXmlUseCase {

    override fun loadNormAsXml(query: LoadNormAsXmlUseCase.Query): Mono<String> {
        return searchNormsAdapter.searchNorms(
            listOf(
                QueryParameter("print_announcement_gazette", query.printAnnouncementGazette),
                QueryParameter("publication_date", query.publicationYear, isYearForDate = true),
                QueryParameter("print_announcement_page", query.printAnnouncementPage)
            )
        ).next().flatMap { norm ->
            convertNormToXmlAdapter.convertNormToXml(norm)
        }
    }
}
