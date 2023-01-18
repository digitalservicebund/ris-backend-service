package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.LoadNormUseCase
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import norms.utils.createRandomNorm
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

class LoadNormServiceTest {
    @Test
    fun `it calls the get norm by GUID adapter using the input query GUID parameter`() {
        val port = mockk<GetNormByGuidOutputPort>()
        val service = LoadNormService(port)
        val guid = UUID.randomUUID()
        val query = LoadNormUseCase.Query(guid)

        every { port.getNormByGuid(any()) } returns Mono.empty()

        service.loadNorm(query).block()

        verify(exactly = 1) { port.getNormByGuid(withArg { assertThat(it.guid).isEqualTo(guid) }) }
    }

    @Test
    fun `can load norm by GUID if output adapter finds one`() {
        val port = mockk<GetNormByGuidOutputPort>()
        val service = LoadNormService(port)
        val query = LoadNormUseCase.Query(UUID.randomUUID())
        val norm = createRandomNorm()

        every { port.getNormByGuid(any()) } returns Mono.just(norm)

        service.loadNorm(query).`as`(StepVerifier::create).expectNext(norm).verifyComplete()
    }

    @Test
    fun `loads nothing if output adapter does not find a norm for a given GUID`() {
        val port = mockk<GetNormByGuidOutputPort>()
        val service = LoadNormService(port)
        val query = LoadNormUseCase.Query(UUID.randomUUID())

        every { port.getNormByGuid(any()) } returns Mono.empty()

        StepVerifier.create(service.loadNorm(query)).expectNextCount(0).verifyComplete()
    }
}
