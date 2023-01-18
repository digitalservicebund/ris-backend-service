package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.LoadNormAsXmlUseCase
import de.bund.digitalservice.ris.norms.application.port.output.ConvertNormToXmlOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByEliOutputPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import norms.utils.createRandomNorm
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate

class LoadNormAsXmlServiceTest {
    private val announcementYear = "2022"
    private val printAnnouncementGazette = "bg-1"
    private val printAnnouncementPage = "1125"

    @Test
    fun `it calls the get norm by ELI output adapter with a query based on the input query`() {
        val getNormByEliAdapter = mockk<GetNormByEliOutputPort>()
        val convertNormToXmlAdapter = mockk<ConvertNormToXmlOutputPort>()
        val service = LoadNormAsXmlService(getNormByEliAdapter, convertNormToXmlAdapter)
        val query = LoadNormAsXmlUseCase.Query("bgbl-1", "2022", "1125")

        every { getNormByEliAdapter.getNormByEli(any()) } returns Mono.empty()
        every { convertNormToXmlAdapter.convertNormToXml(any()) } returns Mono.just("")

        service.loadNormAsXml(query).`as`(StepVerifier::create).expectNextCount(0).verifyComplete()

        verify(exactly = 1) {
            getNormByEliAdapter.getNormByEli(
                withArg {
                    assertThat(it.gazette).isEqualTo("bgbl-1")
                    assertThat(it.year).isEqualTo("2022")
                    assertThat(it.page).isEqualTo("1125")
                }
            )
        }
    }

    @Test
    fun `it returns nothing if norm could not be found by search queries`() {
        val getNormByEliAdapter = mockk<GetNormByEliOutputPort>()
        val convertNormToXmlAdapter = mockk<ConvertNormToXmlOutputPort>()
        val query = LoadNormAsXmlUseCase.Query(printAnnouncementGazette, announcementYear, printAnnouncementPage)
        val service = LoadNormAsXmlService(getNormByEliAdapter, convertNormToXmlAdapter)

        every { getNormByEliAdapter.getNormByEli(any()) } returns Mono.empty()
        every { convertNormToXmlAdapter.convertNormToXml(any()) } returns Mono.just("")

        service.loadNormAsXml(query).`as`(StepVerifier::create).expectNextCount(0).verifyComplete()
    }

    @Test
    fun `it returns output of conversion adapter if norm was found by search queries`() {
        val getNormByEliAdapter = mockk<GetNormByEliOutputPort>()
        val convertNormToXmlAdapter = mockk<ConvertNormToXmlOutputPort>()
        val norm = createRandomNorm()
        norm.printAnnouncementGazette = printAnnouncementGazette
        norm.printAnnouncementPage = printAnnouncementPage
        norm.announcementDate = LocalDate.parse("2022-01-01")
        val query = LoadNormAsXmlUseCase.Query(printAnnouncementGazette, announcementYear, printAnnouncementPage)
        val service = LoadNormAsXmlService(getNormByEliAdapter, convertNormToXmlAdapter)

        every { getNormByEliAdapter.getNormByEli(any()) } returns Mono.just(norm)
        every { convertNormToXmlAdapter.convertNormToXml(any()) } returns Mono.just("fake test xml")

        service
            .loadNormAsXml(query)
            .`as`(StepVerifier::create)
            .expectNext("fake test xml")
            .verifyComplete()
    }

    @Test
    fun `it returns norm by eli that matched announcement date`() {
        val getNormByEliAdapter = mockk<GetNormByEliOutputPort>()
        val convertNormToXmlAdapter = mockk<ConvertNormToXmlOutputPort>()

        val query = LoadNormAsXmlUseCase.Query(printAnnouncementGazette, announcementYear, printAnnouncementPage)
        val service = LoadNormAsXmlService(getNormByEliAdapter, convertNormToXmlAdapter)

        val normWithAnnouncementDate = createRandomNorm()
        normWithAnnouncementDate.printAnnouncementGazette = printAnnouncementGazette
        normWithAnnouncementDate.printAnnouncementPage = printAnnouncementPage
        normWithAnnouncementDate.announcementDate = LocalDate.parse("2022-01-01")

        val normWithCitationDate = createRandomNorm()
        normWithCitationDate.printAnnouncementGazette = printAnnouncementGazette
        normWithCitationDate.printAnnouncementPage = printAnnouncementPage
        normWithCitationDate.citationDate = LocalDate.parse("2022-01-01")

        every { getNormByEliAdapter.getNormByEli(any()) } returns Mono.just(normWithAnnouncementDate)
        every { convertNormToXmlAdapter.convertNormToXml(normWithAnnouncementDate) } returns Mono.just("norm with announcement date")

        service
            .loadNormAsXml(query)
            .`as`(StepVerifier::create)
            .expectNext("norm with announcement date")
            .verifyComplete()
    }
}
