package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.LoadNormUseCase
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class LoadNormService(private val getNormByGuidPort: GetNormByGuidOutputPort) : LoadNormUseCase {

    companion object {
        private val logger = LoggerFactory.getLogger(LoadNormService::class.java)
    }

    override fun loadNorm(query: LoadNormUseCase.Query): Mono<Norm> {
        val guidQuery = GetNormByGuidOutputPort.Query(query.guid)
        return getNormByGuidPort.getNormByGuid(guidQuery).doOnError {
                exception ->
            logger.error("Error occurred while loading a norm:", exception)
        }
    }
}
