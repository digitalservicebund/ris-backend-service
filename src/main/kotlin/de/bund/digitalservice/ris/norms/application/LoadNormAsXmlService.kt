package de.bund.digitalservice.ris.norms.application

import de.bund.digitalservice.ris.norms.application.port.input.LoadNormAsXmlUseCase
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class LoadNormAsXmlService(private val getNormByGuidAdapter: GetNormByGuidOutputPort) :
    LoadNormAsXmlUseCase {

    override fun loadNormAsXml(query: LoadNormAsXmlUseCase.Query): Mono<String> {
        return getNormByGuidAdapter.getNormByGuid(query.guid).map {
            """<?xml version="1.0" encoding="UTF-8"?>"""
        }
    }
}
