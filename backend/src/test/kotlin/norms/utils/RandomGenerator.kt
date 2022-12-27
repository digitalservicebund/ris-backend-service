package norms.utils

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller.EditNormFrameController
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller.ImportNormController
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.jeasy.random.FieldPredicates.named
import java.time.LocalDate
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
        EasyRandomParameters().randomize(named("marker")) {
            createRandomStringWithMaxLength(20)
        } // needed for marker fields
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
        } // needed for string date fields
    val importNormRequestSchema =
        EasyRandom(parameters).nextObject(ImportNormController.NormRequestSchema::class.java)
    importNormRequestSchema.articles = createRandomListOfArticleRequestSchema()
    return importNormRequestSchema
}

private fun createRandomLocalDateInString(): String {
    return EasyRandom().nextObject(LocalDate::class.java).toString()
}

private fun createRandomStringWithMaxLength(maxLength: Int): String {
    val parameters: EasyRandomParameters = EasyRandomParameters().stringLengthRange(1, maxLength)
    return EasyRandom(parameters).nextObject(String::class.java)
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
