package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.LoadNormAsXmlUseCase
import de.bund.digitalservice.ris.norms.application.port.output.ConvertNormToXmlOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort.QueryFields
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort.QueryParameter
import de.bund.digitalservice.ris.norms.domain.value.EuropeanLegalIdentifier
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
                QueryParameter(QueryFields.PRINT_ANNOUNCEMENT_GAZETTE, EuropeanLegalIdentifier.parseGazette(query.printAnnouncementGazette)),
                QueryParameter(QueryFields.ANNOUNCEMENT_DATE, query.announcementOrCitationYear, isYearForDate = true),
                QueryParameter(QueryFields.PRINT_ANNOUNCEMENT_PAGE, query.printAnnouncementPage)
            )
        ).or {
            searchNormsAdapter.searchNorms(
                listOf(
                    QueryParameter(QueryFields.PRINT_ANNOUNCEMENT_GAZETTE, EuropeanLegalIdentifier.parseGazette(query.printAnnouncementGazette)),
                    QueryParameter(QueryFields.ANNOUNCEMENT_DATE, null),
                    QueryParameter(QueryFields.CITATION_DATE, query.announcementOrCitationYear, isYearForDate = true),
                    QueryParameter(QueryFields.PRINT_ANNOUNCEMENT_PAGE, query.printAnnouncementPage)
                )
            )
        }.next().flatMap { norm ->
            convertNormToXmlAdapter.convertNormToXml(norm)
        }
    }
}
