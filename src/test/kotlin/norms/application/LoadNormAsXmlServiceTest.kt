package de.bund.digitalservice.ris.norms.application

import de.bund.digitalservice.ris.norms.application.port.input.LoadNormAsXmlUseCase
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import io.mockk.every
import io.mockk.mockk
import norms.utils.createRandomNorm
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

class LoadNormAsXmlServiceTest {
    @Test
    fun `it returns nothing if norm could not be found by its GUID`() {
        val getNormByGuidAdapter = mockk<GetNormByGuidOutputPort>()
        val guid = UUID.randomUUID()
        val query = LoadNormAsXmlUseCase.Query(guid)
        val service = LoadNormAsXmlService(getNormByGuidAdapter)

        every { getNormByGuidAdapter.getNormByGuid(guid) } returns Mono.empty()

        service.loadNormAsXml(query).`as`(StepVerifier::create).expectNextCount(0).verifyComplete()
    }

    @Test
    fun `it returns a string if norm was found by its GUID`() {
        val getNormByGuidAdapter = mockk<GetNormByGuidOutputPort>()
        val guid = UUID.randomUUID()
        val norm = createRandomNorm()
        val query = LoadNormAsXmlUseCase.Query(guid)
        val service = LoadNormAsXmlService(getNormByGuidAdapter)

        every { getNormByGuidAdapter.getNormByGuid(guid) } returns Mono.just(norm)

        service
            .loadNormAsXml(query)
            .`as`(StepVerifier::create)
            .expectNext("""<?xml version="1.0" encoding="UTF-8"?>""")
            .verifyComplete()
    }
}
