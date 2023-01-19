package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.ListNormsUseCase
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.util.*

class ListNormsServiceTest {
    @Test
    fun `if the search term is missing, it does an empty search for everything`() {
        val searchNormsAdapter = mockk<SearchNormsOutputPort>()
        val service = ListNormsService(searchNormsAdapter)
        val query = ListNormsUseCase.Query(searchTerm = null)

        every { searchNormsAdapter.searchNorms(any()) } returns Flux.empty()

        service.listNorms(query).blockLast()

        verify(exactly = 1) { searchNormsAdapter.searchNorms(withArg { assertThat(it.parameters).hasSize(0) }) }
    }

    @Test
    fun `if the search term is empty, it does an empty search for everything`() {
        val searchNormsAdapter = mockk<SearchNormsOutputPort>()
        val service = ListNormsService(searchNormsAdapter)
        val query = ListNormsUseCase.Query(searchTerm = "")

        every { searchNormsAdapter.searchNorms(any()) } returns Flux.empty()

        service.listNorms(query).blockLast()

        verify(exactly = 1) { searchNormsAdapter.searchNorms(withArg { assertThat(it.parameters).hasSize(0) }) }
    }

    @Test
    fun `if a search term is given, it calls the search adapter with a fuzzy search in all title fields`() {
        val searchNormsAdapter = mockk<SearchNormsOutputPort>()
        val service = ListNormsService(searchNormsAdapter)
        val query = ListNormsUseCase.Query(searchTerm = "test")

        every { searchNormsAdapter.searchNorms(any()) } returns Flux.empty()

        service.listNorms(query).blockLast()

        verify(exactly = 1) {
            searchNormsAdapter.searchNorms(
                withArg {
                    assertThat(it.parameters).hasSize(4)

                    assertThat(it.parameters.map { it.field }).isEqualTo(
                        listOf(
                            SearchNormsOutputPort.QueryFields.OFFICIAL_LONG_TITLE,
                            SearchNormsOutputPort.QueryFields.OFFICIAL_SHORT_TITLE,
                            SearchNormsOutputPort.QueryFields.UNOFFICIAL_LONG_TITLE,
                            SearchNormsOutputPort.QueryFields.UNOFFICIAL_SHORT_TITLE
                        )
                    )

                    it.parameters.forEach {
                        assertThat(it.value).isEqualTo("test")
                        assertThat(it.isFuzzyMatch).isTrue()
                    }
                }
            )
        }
    }

    @Test
    fun `lists nothing if output adapter provides no norms`() {
        val searchNormsAdapter = mockk<SearchNormsOutputPort>()
        val service = ListNormsService(searchNormsAdapter)
        val query = ListNormsUseCase.Query()

        every { searchNormsAdapter.searchNorms(any()) } returns Flux.empty()

        service.listNorms(query).`as`(StepVerifier::create).expectNextCount(0).verifyComplete()
    }

    @Test
    fun `lists single norm if output adapter provides only one`() {
        val searchNormsAdapter = mockk<SearchNormsOutputPort>()
        val service = ListNormsService(searchNormsAdapter)
        val norm =
            Norm(
                UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"),
                officialLongTitle = "title"
            )
        val query = ListNormsUseCase.Query()

        every { searchNormsAdapter.searchNorms(any()) } returns Flux.fromArray(arrayOf(norm))

        service.listNorms(query).`as`(StepVerifier::create)
            .expectNextMatches({
                it.guid == UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8") &&
                    it.officialLongTitle == "title" &&
                    it.eli == norm.eli
            })
            .verifyComplete()
    }

    @Test
    fun `continuously lists norms from output adapter if there are multiple`() {
        val searchNormsAdapter = mockk<SearchNormsOutputPort>()
        val service = ListNormsService(searchNormsAdapter)
        val normOne =
            Norm(
                UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"),
                officialLongTitle = "title one"
            )
        val normTwo =
            Norm(
                UUID.fromString("53d29ef7-377c-4d14-864b-eb3a85769359"),
                officialLongTitle = "title two"
            )
        val normThree =
            Norm(
                UUID.fromString("2c7da53b-1d57-46b4-90b2-96bd746c268a"),
                officialLongTitle = "title three"
            )
        val query = ListNormsUseCase.Query()

        every { searchNormsAdapter.searchNorms(any()) } returns
            Flux.fromArray(arrayOf(normOne, normTwo, normThree))

        service.listNorms(query).`as`(StepVerifier::create)
            .expectNextMatches({
                it.guid == UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8") &&
                    it.officialLongTitle == "title one"
            })
            .expectNextMatches({
                it.guid == UUID.fromString("53d29ef7-377c-4d14-864b-eb3a85769359") &&
                    it.officialLongTitle == "title two"
            })
            .expectNextMatches({
                it.guid == UUID.fromString("2c7da53b-1d57-46b4-90b2-96bd746c268a") &&
                    it.officialLongTitle == "title three"
            })
            .verifyComplete()
    }
}
