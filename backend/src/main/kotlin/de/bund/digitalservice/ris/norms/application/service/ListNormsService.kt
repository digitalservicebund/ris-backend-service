package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.ListNormsUseCase
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class ListNormsService(private val searchNormsOutputAdapter: SearchNormsOutputPort) : ListNormsUseCase {

    companion object {
        private val logger = LoggerFactory.getLogger(ListNormsService::class.java)
    }

    override fun listNorms(query: ListNormsUseCase.Query): Flux<ListNormsUseCase.NormData> {
        val searchQuery = if (query.searchTerm.isNullOrBlank()) {
            SearchNormsOutputPort.Query(emptyList())
        } else {
            createSearchTermQuery(query.searchTerm)
        }

        return searchNormsOutputAdapter.searchNorms(searchQuery).map { mapToNormData(it) }
            .doOnError { exception ->
                logger.error("Error occurred while listing all norms:", exception)
            }
    }
}

// TODO RISDEV-1813 Add UNOFFICIAL_LONG_TITLE & UNOFFICIAL_SHORT_TITLE once all metadata are migrated
private val SEARCH_TERM_QUERY_FIELDS = listOf(
    SearchNormsOutputPort.QueryFields.OFFICIAL_LONG_TITLE,
    SearchNormsOutputPort.QueryFields.OFFICIAL_SHORT_TITLE,
)

private fun createSearchTermQuery(term: String): SearchNormsOutputPort.Query {
    val parameters = SEARCH_TERM_QUERY_FIELDS.map {
        SearchNormsOutputPort.QueryParameter(it, term, isFuzzyMatch = true)
    }

    return SearchNormsOutputPort.Query(parameters)
}

private fun mapToNormData(norm: Norm) = ListNormsUseCase.NormData(
    norm.guid,
    norm.officialLongTitle,
    norm.eli,
)
