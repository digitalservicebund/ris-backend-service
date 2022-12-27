package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.output.GetAllNormsOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.util.*

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
        val norm = Norm(UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"), officialLongTitle = "title")

        every { port.getAllNorms() } returns Flux.fromArray(arrayOf(norm))

        StepVerifier.create(service.listNorms())
            .expectNextMatches({
                it.guid == UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8") &&
                    it.officialLongTitle == "title"
            })
            .verifyComplete()
    }

    @Test
    fun `continuously lists norms from output adapter if there are multiple`() {
        val port = mockk<GetAllNormsOutputPort>()
        val service = ListNormsService(port)
        val normOne = Norm(UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"), officialLongTitle = "title one")
        val normTwo = Norm(UUID.fromString("53d29ef7-377c-4d14-864b-eb3a85769359"), officialLongTitle = "title two")
        val normThree = Norm(UUID.fromString("2c7da53b-1d57-46b4-90b2-96bd746c268a"), officialLongTitle = "title three")

        every { port.getAllNorms() } returns Flux.fromArray(arrayOf(normOne, normTwo, normThree))

        StepVerifier.create(service.listNorms())
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
