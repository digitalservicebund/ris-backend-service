package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.ImportTestDataUseCase
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import utils.createRandomNorm

class ImportTestDataServicTest {
  @Test
  fun `it forwards the norm to the save norm adapter`() {
    val saveNormAdapter = mockk<SaveNormOutputPort>()
    val service = ImportTestDataService(saveNormAdapter)
    val norm = createRandomNorm()
    val command = ImportTestDataUseCase.Command(norm)

    every { saveNormAdapter.saveNorm(any()) } returns Mono.empty()

    service.importTestData(command).block()

    verify(exactly = 1) {
      saveNormAdapter.saveNorm(withArg { assertThat(it.norm).isEqualTo(norm) })
    }
  }

  @Test
  fun `it forwards the result of the save norm adapter`() {
    val saveNormAdapter = mockk<SaveNormOutputPort>()
    val service = ImportTestDataService(saveNormAdapter)
    val norm = createRandomNorm()
    val command = ImportTestDataUseCase.Command(norm)

    every { saveNormAdapter.saveNorm(any()) } returns Mono.just(true)
    service
        .importTestData(command)
        .`as`(StepVerifier::create)
        .expectNextMatches { it == true }
        .verifyComplete()

    every { saveNormAdapter.saveNorm(any()) } returns Mono.just(false)
    service
        .importTestData(command)
        .`as`(StepVerifier::create)
        .expectNextMatches { it == false }
        .verifyComplete()

    every { saveNormAdapter.saveNorm(any()) } returns Mono.empty()
    service.importTestData(command).`as`(StepVerifier::create).expectNextCount(0).verifyComplete()
  }
}
