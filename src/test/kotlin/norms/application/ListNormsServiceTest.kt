package de.bund.digitalservice.ris.norms.application

import de.bund.digitalservice.ris.norms.application.port.output.GetAllNormsOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.value.Guid
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

// TODO: Improve by using behavior driven testing concept with documentation.
class ListNormsServiceTest {
    @Test
    fun `lists nothing if output adapter provides no norms`() {
        val port = mockk<GetAllNormsOutputPort>()
        val service = ListNormsService(port)

        every { port.getAllNorms() } returns Flux.empty()

        StepVerifier.create(service.listNorms()).expectNextCount(0).verifyComplete()
    }

    @Test
    fun `lists single norm if output adapter provides only one`() {
        val port = mockk<GetAllNormsOutputPort>()
        val service = ListNormsService(port)
        val norm = Norm(Guid.generateNew(), "title")

        every { port.getAllNorms() } returns Flux.fromArray(arrayOf(norm))

        StepVerifier.create(service.listNorms()).expectNext(norm).verifyComplete()
    }

    @Test
    fun `continuously lists norms from output adapter if there are multiple`() {
        val port = mockk<GetAllNormsOutputPort>()
        val service = ListNormsService(port)
        val normOne = Norm(Guid.generateNew(), "title one")
        val normTwo = Norm(Guid.generateNew(), "title two")
        val normThree = Norm(Guid.generateNew(), "title three")

        every { port.getAllNorms() } returns Flux.fromArray(arrayOf(normOne, normTwo, normThree))

        StepVerifier.create(service.listNorms())
            .expectNext(normOne)
            .expectNext(normTwo)
            .expectNext(normThree)
            .verifyComplete()
    }
}
