package de.bund.digitalservice.ris.norms.application.port.input

import reactor.core.publisher.Mono
import java.io.File
import java.util.UUID

interface ImportNormUseCase {
    fun importNorm(command: Command): Mono<UUID>

    data class Command(val zipFile: File) }
