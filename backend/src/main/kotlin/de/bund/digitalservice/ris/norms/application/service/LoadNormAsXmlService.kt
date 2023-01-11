package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.LoadNormAsXmlUseCase
import de.bund.digitalservice.ris.norms.application.port.output.ConvertNormToXmlOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort.QueryFields
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
                QueryParameter(QueryFields.PRINT_ANNOUNCEMENT_GAZETTE, transformEliGazetteToDbGazette(query.printAnnouncementGazette)),
                QueryParameter(QueryFields.ANNOUNCEMENT_OR_CITATION_YEAR, query.announcementOrCitationYear, isYearForDate = true),
                QueryParameter(QueryFields.PRINT_ANNOUNCEMENT_PAGE, query.printAnnouncementPage)
            )
        ).filter { norm ->
            (
                (norm.announcementDate != null && norm.announcementDate?.year.toString() == query.announcementOrCitationYear) ||
                    (norm.announcementDate == null && norm.citationDate?.year.toString() == query.announcementOrCitationYear)
                )
        }.next().flatMap { norm ->
            convertNormToXmlAdapter.convertNormToXml(norm)
        }
    }

    private fun transformEliGazetteToDbGazette(gazette: String): String {
        return when (gazette) {
            "bgbl-1" -> "BGBl I"
            "bgbl-2" -> "BGBl II"
            "banz-at" -> "BAnz"
            else -> {
                gazette
            }
        }
    }
}
