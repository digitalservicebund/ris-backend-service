package de.bund.digitalservice.ris.norms.application

import de.bund.digitalservice.ris.norms.application.port.input.LoadNormUseCase
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

// TODO: Improve by using behavior driven testing concept with documentation.
class LoadNormServiceTest {
    @Test
    fun `can load norm by GUID if output adapter finds one`() {
        val port = mockk<GetNormByGuidOutputPort>()
        val service = LoadNormService(port)
        val guid = UUID.randomUUID()
        val query = LoadNormUseCase.Query(guid)
        val norm = Norm(guid, "title")

        every { port.getNormByGuid(guid) } returns Mono.just(norm)

        StepVerifier.create(service.loadNorm(query)).expectNext(norm).verifyComplete()
    }

    @Test
    fun `loads nothing if output adapter does not find a norm for a given GUID`() {
        val port = mockk<GetNormByGuidOutputPort>()
        val service = LoadNormService(port)
        val guid = UUID.randomUUID()
        val query = LoadNormUseCase.Query(guid)

        every { port.getNormByGuid(guid) } returns Mono.empty()

        StepVerifier.create(service.loadNorm(query)).expectNextCount(0).verifyComplete()
    }
}
