package de.bund.digitalservice.ris.norms.framework.adapter.output.juris

import de.bund.digitalservice.ris.norms.application.port.output.GenerateNormFileOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.ParseJurisArrayOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.ParseJurisXmlOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.getHashFromContent
import de.bund.digitalservice.ris.norms.juris.converter.extractor.extractData
import de.bund.digitalservice.ris.norms.juris.converter.generator.generateZip
import java.nio.ByteBuffer
import java.util.*
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class JurisConverter() :
    ParseJurisXmlOutputPort, GenerateNormFileOutputPort, ParseJurisArrayOutputPort {
  override fun parseJurisXml(query: ParseJurisXmlOutputPort.Query): Mono<Norm> {
    val data = extractData(ByteBuffer.wrap(query.zipFile))
    val norm = mapDataToDomain(query.newGuid, data)
    norm.files =
        listOf(
            FileReference(
                query.filename, getHashFromContent(query.zipFile), guid = UUID.randomUUID()))
    return Mono.just(norm)
  }

  override fun parseJurisArray(query: ParseJurisArrayOutputPort.Query): Flux<Norm> {
    return Flux.fromIterable(query.norms.map { mapDataToDomain(it.guid, it.norm) })
  }

  override fun generateNormFile(command: GenerateNormFileOutputPort.Command): Mono<ByteArray> {
    return Mono.just(
        generateZip(mapDomainToData(command.norm), ByteBuffer.wrap(command.previousFile)))
  }
}
