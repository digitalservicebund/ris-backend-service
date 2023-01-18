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

class LoadNormAsXmlServiceTest {
    private val anyQuery = LoadNormAsXmlUseCase.Query("bg-1", "2022", "1125")

    @Test
    fun `it calls the get norm by ELI output adapter with a query based on the input query`() {
        val getNormByEliAdapter = mockk<GetNormByEliOutputPort>()
        val convertNormToXmlAdapter = mockk<ConvertNormToXmlOutputPort>()
        val service = LoadNormAsXmlService(getNormByEliAdapter, convertNormToXmlAdapter)
        val query = LoadNormAsXmlUseCase.Query("bgbl-1", "2022", "1125")

        every { getNormByEliAdapter.getNormByEli(any()) } returns Mono.empty()
        every { convertNormToXmlAdapter.convertNormToXml(any()) } returns Mono.just("")

        service.loadNormAsXml(query).block()

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
    fun `it calls the convert norm to XML output adapter with the found norm`() {
        val getNormByEliAdapter = mockk<GetNormByEliOutputPort>()
        val convertNormToXmlAdapter = mockk<ConvertNormToXmlOutputPort>()
        val service = LoadNormAsXmlService(getNormByEliAdapter, convertNormToXmlAdapter)
        val norm = createRandomNorm()

        every { getNormByEliAdapter.getNormByEli(any()) } returns Mono.just(norm)
        every { convertNormToXmlAdapter.convertNormToXml(any()) } returns Mono.just("")

        service.loadNormAsXml(anyQuery).block()

        verify(exactly = 1) {
            convertNormToXmlAdapter.convertNormToXml(
                withArg {
                    assertThat(it.norm).isEqualTo(norm)
                }
            )
        }
    }

    @Test
    fun `it does not call the convert norm to XML output adapter if no norm can be found`() {
        val getNormByEliAdapter = mockk<GetNormByEliOutputPort>()
        val convertNormToXmlAdapter = mockk<ConvertNormToXmlOutputPort>()
        val service = LoadNormAsXmlService(getNormByEliAdapter, convertNormToXmlAdapter)

        every { getNormByEliAdapter.getNormByEli(any()) } returns Mono.empty()
        every { convertNormToXmlAdapter.convertNormToXml(any()) } returns Mono.just("")

        service.loadNormAsXml(anyQuery).block()

        verify(exactly = 0) { convertNormToXmlAdapter.convertNormToXml(any()) }
    }

    @Test
    fun `it returns nothing if no norm could not be found by ELI`() {
        val getNormByEliAdapter = mockk<GetNormByEliOutputPort>()
        val convertNormToXmlAdapter = mockk<ConvertNormToXmlOutputPort>()
        val service = LoadNormAsXmlService(getNormByEliAdapter, convertNormToXmlAdapter)

        every { getNormByEliAdapter.getNormByEli(any()) } returns Mono.empty()
        every { convertNormToXmlAdapter.convertNormToXml(any()) } returns Mono.just("")

        service.loadNormAsXml(anyQuery).`as`(StepVerifier::create).expectNextCount(0).verifyComplete()
    }

    @Test
    fun `it returns output of conversion adapter if norm was found by ELI`() {
        val getNormByEliAdapter = mockk<GetNormByEliOutputPort>()
        val convertNormToXmlAdapter = mockk<ConvertNormToXmlOutputPort>()
        val service = LoadNormAsXmlService(getNormByEliAdapter, convertNormToXmlAdapter)
        val norm = createRandomNorm()

        every { getNormByEliAdapter.getNormByEli(any()) } returns Mono.just(norm)
        every { convertNormToXmlAdapter.convertNormToXml(any()) } returns Mono.just("fake test xml")

        service
            .loadNormAsXml(anyQuery)
            .`as`(StepVerifier::create)
            .expectNext("fake test xml")
            .verifyComplete()
    }
}
