package de.bund.digitalservice.ris.norms.application.port.input

import reactor.core.publisher.Mono
import java.util.UUID

interface ImportNormUseCase {
    fun importNorm(command: Command): Mono<UUID>

    data class Command(val data: NormData)

    data class NormData(val longTitle: String, val articles: List<ArticleData>) {
        init {
            require(longTitle.isNotBlank())
        }
    }

    data class ArticleData(
        val title: String,
        val marker: String,
        val paragraphs: List<ParagraphData>
    ) {
        init {
            require(title.isNotBlank())
            require(marker.isNotBlank())
        }
    }

    data class ParagraphData(val marker: String, val text: String) {
        init {
            require(marker.isNotBlank())
            require(text.isNotBlank())
        }
    }
}
