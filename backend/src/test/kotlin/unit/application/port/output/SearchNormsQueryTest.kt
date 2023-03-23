package de.bund.digitalservice.ris.norms.application.port.output

import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort.QueryFields
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort.QueryParameter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SearchNormsQueryTest {

    @Test
    fun `can create an empty query`() {
        val query = SearchNormsOutputPort.Query(emptyList())

        assertThat(query.parameters).hasSize(0)
    }

    @Test
    fun `can create query with a all available fields`() {
        val parameters = mutableListOf<QueryParameter>()
        parameters.add(QueryParameter(QueryFields.PRINT_ANNOUNCEMENT_GAZETTE, "test gazette"))
        parameters.add(QueryParameter(QueryFields.ANNOUNCEMENT_DATE, "test value"))
        parameters.add(QueryParameter(QueryFields.CITATION_DATE, "test value"))
        parameters.add(QueryParameter(QueryFields.PRINT_ANNOUNCEMENT_PAGE, "test value"))
        parameters.add(QueryParameter(QueryFields.OFFICIAL_LONG_TITLE, "test value"))
        parameters.add(QueryParameter(QueryFields.OFFICIAL_SHORT_TITLE, "test value"))

        val query = SearchNormsOutputPort.Query(parameters)

        assertThat(query.parameters).isEqualTo(parameters)
    }

    @Test
    fun `can create query with explicit Null value`() {
        val nullParameter = QueryParameter(QueryFields.OFFICIAL_LONG_TITLE, null)
        val query = SearchNormsOutputPort.Query(listOf(nullParameter))

        assertThat(query.parameters[0].value).isNull()
    }

    @Test
    fun `can create query with fuzzy matching enabled`() {
        val fuzzyParameter =
            QueryParameter(QueryFields.OFFICIAL_LONG_TITLE, "value", isFuzzyMatch = true)
        val query = SearchNormsOutputPort.Query(listOf(fuzzyParameter))

        assertThat(query.parameters[0].isFuzzyMatch).isTrue()
    }
}
