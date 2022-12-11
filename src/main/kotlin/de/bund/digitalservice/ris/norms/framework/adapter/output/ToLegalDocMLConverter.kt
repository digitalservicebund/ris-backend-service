package de.bund.digitalservice.ris.norms.framework.adapter.output

import de.bund.digitalservice.ris.norms.application.port.output.ConvertNormToXmlOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ToLegalDocMLConverter : ConvertNormToXmlOutputPort {
    override fun convertNormToXml(norm: Norm): Mono<String> {
        return Mono.just("""<?xml version="1.0" encoding="utf-8"?>""")
    }
}
