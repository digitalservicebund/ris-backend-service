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
    private val announcementYear = "2022"
    private val printAnnouncementGazette = "bg-1"
    private val printAnnouncementPage = "1125"
    private val searchNormQuery = listOf(
        QueryParameter(QueryFields.PRINT_ANNOUNCEMENT_GAZETTE, printAnnouncementGazette),
        QueryParameter(QueryFields.ANNOUNCEMENT_OR_CITATION_YEAR, announcementYear, isYearForDate = true),
        QueryParameter(QueryFields.PRINT_ANNOUNCEMENT_PAGE, printAnnouncementPage)
    )

    @Test
    fun `it returns nothing if norm could not be found by search queries`() {
        val searchNormAdapter = mockk<SearchNormsOutputPort>()
        val convertNormToXmlAdapter = mockk<ConvertNormToXmlOutputPort>()
        val query = LoadNormAsXmlUseCase.Query(printAnnouncementGazette, announcementYear, printAnnouncementPage)
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
        norm.announcementDate = LocalDate.parse("2022-01-01")
        val query = LoadNormAsXmlUseCase.Query(printAnnouncementGazette, announcementYear, printAnnouncementPage)
        val service = LoadNormAsXmlService(searchNormAdapter, convertNormToXmlAdapter)

        every { searchNormAdapter.searchNorms(searchNormQuery) } returns Flux.just(norm)
        every { convertNormToXmlAdapter.convertNormToXml(any()) } returns Mono.just("fake test xml")

        service
            .loadNormAsXml(query)
            .`as`(StepVerifier::create)
            .expectNext("fake test xml")
            .verifyComplete()
    }

    @Test
    fun `it returns norm by eli that matched announcement date`() {
        val searchNormAdapter = mockk<SearchNormsOutputPort>()
        val convertNormToXmlAdapter = mockk<ConvertNormToXmlOutputPort>()

        val query = LoadNormAsXmlUseCase.Query(printAnnouncementGazette, announcementYear, printAnnouncementPage)
        val service = LoadNormAsXmlService(searchNormAdapter, convertNormToXmlAdapter)

        val normWithAnnouncementDate = createRandomNorm()
        normWithAnnouncementDate.printAnnouncementGazette = printAnnouncementGazette
        normWithAnnouncementDate.printAnnouncementPage = printAnnouncementPage
        normWithAnnouncementDate.announcementDate = LocalDate.parse("2022-01-01")

        val normWithCitationDate = createRandomNorm()
        normWithCitationDate.printAnnouncementGazette = printAnnouncementGazette
        normWithCitationDate.printAnnouncementPage = printAnnouncementPage
        normWithCitationDate.citationDate = LocalDate.parse("2022-01-01")

        every { searchNormAdapter.searchNorms(searchNormQuery) } returns Flux.just(normWithAnnouncementDate, normWithCitationDate)
        every { convertNormToXmlAdapter.convertNormToXml(normWithAnnouncementDate) } returns Mono.just("norm with announcement date")
        every { convertNormToXmlAdapter.convertNormToXml(normWithCitationDate) } returns Mono.just("norm with citation date")

        service
            .loadNormAsXml(query)
            .`as`(StepVerifier::create)
            .expectNext("norm with announcement date")
            .verifyComplete()
    }
}
