package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.norms.application.port.output.GetAllNormsOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ArticleDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ParagraphDto
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class NormsDatabaseRepository(
    val normsRepository: NormsRepository,
    val articlesRepository: ArticlesRepository,
    val paragraphsRepository: ParagraphsRepository
) : GetAllNormsOutputPort, GetNormByGuidOutputPort, SaveNormOutputPort {

    override fun getNormByGuid(guid: UUID): Mono<Norm> {
        return normsRepository
            .findByGuid(guid)
            .flatMap { normDto: NormDto ->
                articlesRepository
                    .findByNorm(normDto.id)
                    .flatMap { articleDto: ArticleDto ->
                        paragraphsRepository
                            .findByArticle(articleDto.id)
                            .collectList()
                            .flatMap { paragraphs: List<ParagraphDto> -> Mono.just(articleDto.setParagraphs(paragraphs)) }
                    }
                    .collectList()
                    .flatMap { articles: List<ArticleDto> -> Mono.just(normDto.setArticles(articles)) }
            }
            // TODO convert ArticleDTO and ParagraphDTO to Article and Paragraph
            .map { normDto: NormDto -> Norm(normDto.guid, normDto.longTitle, listOf()) }
    }

    override fun getAllNorms(): Flux<Norm> {
        return Flux.empty()
    }

    override fun saveNorm(norm: Norm): Mono<Boolean> {
        normsRepository.save(NormDto(0, norm.guid, norm.longTitle)).map { normRecord ->
            norm.articles.forEach { article ->
                articlesRepository.save(
                    ArticleDto(0, article.guid, article.title, article.marker, normRecord.id)
                ).map { articleRecord ->
                    article.paragraphs.forEach { paragraph ->
                        paragraphsRepository.save(
                            ParagraphDto(
                                0,
                                paragraph.guid,
                                paragraph.marker,
                                paragraph.text,
                                articleRecord.id
                            )
                        )
                    }
                }
            }
        }
        return Mono.just(true)
    }
}
