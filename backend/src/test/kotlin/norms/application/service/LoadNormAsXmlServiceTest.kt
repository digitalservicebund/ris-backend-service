package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.LoadNormAsXmlUseCase
import de.bund.digitalservice.ris.norms.application.port.output.ConvertNormToXmlOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort.QueryFields
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort.QueryParameter
import io.mockk.every
import io.mockk.mockk
import norms.utils.createRandomNorm
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate

class LoadNormAsXmlServiceTest {
    private val publicationYear = "2022"
    private val printAnnouncementGazette = "bg-1"
    private val printAnnouncementPage = "1125"
    private val searchNormQuery = listOf(
        QueryParameter(QueryFields.PRINT_ANNOUNCEMENT_GAZETTE, printAnnouncementGazette),
        QueryParameter(QueryFields.PUBLICATION_YEAR, publicationYear, isYearForDate = true),
        QueryParameter(QueryFields.PRINT_ANNOUNCEMENT_PAGE, printAnnouncementPage)
    )

    @Test
    fun `it returns nothing if norm could not be found by search queries`() {
        val searchNormAdapter = mockk<SearchNormsOutputPort>()
        val convertNormToXmlAdapter = mockk<ConvertNormToXmlOutputPort>()
        val query = LoadNormAsXmlUseCase.Query(printAnnouncementGazette, publicationYear, printAnnouncementPage)
        val service = LoadNormAsXmlService(searchNormAdapter, convertNormToXmlAdapter)

        every { searchNormAdapter.searchNorms(searchNormQuery) } returns Flux.empty()
        every { convertNormToXmlAdapter.convertNormToXml(any()) } returns Mono.just("")

        service.loadNormAsXml(query).`as`(StepVerifier::create).expectNextCount(0).verifyComplete()
    }

    @Test
    fun `it returns output of conversion adapter if norm was found by search queries`() {
        val searchNormAdapter = mockk<SearchNormsOutputPort>()
        val convertNormToXmlAdapter = mockk<ConvertNormToXmlOutputPort>()
        val norm = createRandomNorm()
        norm.printAnnouncementGazette = printAnnouncementGazette
        norm.printAnnouncementPage = printAnnouncementPage
        norm.publicationDate = LocalDate.parse("2022-01-01")
        val query = LoadNormAsXmlUseCase.Query(printAnnouncementGazette, publicationYear, printAnnouncementPage)
        val service = LoadNormAsXmlService(searchNormAdapter, convertNormToXmlAdapter)

        every { searchNormAdapter.searchNorms(searchNormQuery) } returns Flux.just(norm)
        every { convertNormToXmlAdapter.convertNormToXml(any()) } returns Mono.just("fake test xml")

        service
            .loadNormAsXml(query)
            .`as`(StepVerifier::create)
            .expectNext("fake test xml")
            .verifyComplete()
    }
}
