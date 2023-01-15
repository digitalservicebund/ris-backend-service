package norms.utils

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller.EditNormFrameController
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller.ImportNormController
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.jeasy.random.FieldPredicates.inClass
import org.jeasy.random.FieldPredicates.named
import java.time.LocalDate
import java.util.Random
import java.util.stream.Collectors

fun createRandomNormFameProperties(): EditNormFrameUseCase.NormFrameProperties {
    return EasyRandom().nextObject(EditNormFrameUseCase.NormFrameProperties::class.java)
}

fun createRandomEditNormRequestSchema(): EditNormFrameController.NormFramePropertiesRequestSchema {
    val parameters: EasyRandomParameters =
        EasyRandomParameters().randomize(named(".+Date\$")) {
            createRandomLocalDateInString()
        } // needed for string date fields
    return EasyRandom(parameters)
        .nextObject(EditNormFrameController.NormFramePropertiesRequestSchema::class.java)
}

fun createRandomNorm(): Norm {
    val parameters: EasyRandomParameters =
        EasyRandomParameters().randomize(named("marker").and(inClass(Article::class.java))) {
            "§ " + Random().nextInt(1, 50).toString()
        }.randomize(named("marker").and(inClass(Paragraph::class.java))) {
            "(" + Random().nextInt(1, 50).toString() + ")"
        }
    return EasyRandom(parameters).nextObject(Norm::class.java)
}

fun createRandomImportNormData(): ImportNormUseCase.NormData {
    val parameters: EasyRandomParameters = EasyRandomParameters().collectionSizeRange(2, 2)
    return EasyRandom(parameters).nextObject(ImportNormUseCase.NormData::class.java)
}

fun createRandomImportNormRequestSchema(): ImportNormController.NormRequestSchema {
    val parameters: EasyRandomParameters =
        EasyRandomParameters().randomize(named(".+Date\$")) {
            createRandomLocalDateInString()
        }.randomize(named(".+DateState\$")) {
            createRandomUndefinedDate()
        }
    val importNormRequestSchema =
        EasyRandom(parameters).nextObject(ImportNormController.NormRequestSchema::class.java)
    importNormRequestSchema.articles = createRandomListOfArticleRequestSchema()
    return importNormRequestSchema
}

private fun createRandomUndefinedDate(): String {
    return EasyRandom().nextObject(UndefinedDate::class.java).toString()
}

private fun createRandomLocalDateInString(): String {
    return EasyRandom().nextObject(LocalDate::class.java).toString()
}

private fun createRandomListOfArticleRequestSchema():
    List<ImportNormController.ArticleRequestSchema> {
    val articles =
        EasyRandom()
            .objects(ImportNormController.ArticleRequestSchema::class.java, 2)
            .collect(Collectors.toList())
    articles.forEach { it.paragraphs = createRandomListOfParagraphRequestSchema() }
    return articles
}

private fun createRandomListOfParagraphRequestSchema():
    List<ImportNormController.ParagraphRequestSchema> {
    return EasyRandom()
        .objects(ImportNormController.ParagraphRequestSchema::class.java, 2)
        .collect(Collectors.toList())
}
