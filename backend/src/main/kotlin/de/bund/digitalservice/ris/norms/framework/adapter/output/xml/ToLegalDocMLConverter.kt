package de.bund.digitalservice.ris.norms.framework.adapter.output.xml

import com.github.mustachejava.DefaultMustacheFactory
import de.bund.digitalservice.ris.norms.application.port.output.ConvertNormToXmlOutputPort
import java.io.StringWriter
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ToLegalDocMLConverter : ConvertNormToXmlOutputPort {
  override fun convertNormToXml(command: ConvertNormToXmlOutputPort.Command): Mono<String> {
    val writer = StringWriter()
    DefaultMustacheFactory()
        .compile("legaldocml/templates/regular-form.mustache")
        .execute(writer, mapNormToDto(command.norm))
        .flush()
    return Mono.just(writer.toString())
  }
}
