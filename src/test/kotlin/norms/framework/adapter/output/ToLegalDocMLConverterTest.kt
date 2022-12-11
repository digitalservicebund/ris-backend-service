package de.bund.digitalservice.ris.norms.framework.adapter.output

import norms.utils.createRandomNorm
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier

class ToLegalDocMLConverterTest {
    @Test
    fun `it returns a static simple XML when converting a norm`() {
        val norm = createRandomNorm()
        val converter = ToLegalDocMLConverter()

        converter
            .convertNormToXml(norm)
            .`as`(StepVerifier::create)
            .expectNext("""<?xml version="1.0" encoding="utf-8"?>""")
            .verifyComplete()
    }
}
