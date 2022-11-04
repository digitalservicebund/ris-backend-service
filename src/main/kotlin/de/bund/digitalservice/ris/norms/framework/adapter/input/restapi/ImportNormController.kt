package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.net.URI

@RestController
@RequestMapping(ApiConfiguration.API_BASE_PATH)
class ImportNormController(private val importNormService: ImportNormUseCase) {
    @PostMapping
    fun createNorm(@RequestBody resource: NormRequestSchema): Mono<ResponseEntity<Void>> {
        val command = ImportNormUseCase.Command(resource.toUseCaseData())

        return importNormService
            .importNorm(command)
            .map({ guid -> URI("${ApiConfiguration.API_BASE_PATH}/$guid") })
            .map({ uri -> ResponseEntity.created(uri).build<Void>() })
            .onErrorReturn(ResponseEntity.internalServerError().build())
    }

    class NormRequestSchema {
        lateinit var longTitle: String
        var articles: List<ArticleRequestSchema> = listOf()

        fun toUseCaseData(): ImportNormUseCase.NormData {
            return ImportNormUseCase.NormData(longTitle, articles.map { it.toUseCaseData() })
        }
    }

    class ArticleRequestSchema {
        lateinit var title: String
        lateinit var marker: String
        var paragraphs: List<ParagraphRequestSchema> = listOf()

        fun toUseCaseData(): ImportNormUseCase.ArticleData {
            return ImportNormUseCase.ArticleData(title, marker, paragraphs.map { it.toUseCaseData() })
        }
    }

    class ParagraphRequestSchema {
        lateinit var marker: String
        lateinit var text: String

        fun toUseCaseData(): ImportNormUseCase.ParagraphData {
            return ImportNormUseCase.ParagraphData(marker, text)
        }
    }
}
