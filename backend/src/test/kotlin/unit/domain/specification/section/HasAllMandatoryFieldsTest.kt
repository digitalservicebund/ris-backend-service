package de.bund.digitalservice.ris.norms.domain.specification.section

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.specification.SpecificationViolation
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.NormCategory
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasAllMandatoryFieldsTest {
  @Test
  fun `it is satisfied if all mandatory fields are given`() {
    val normSection =
        MetadataSection(
            MetadataSectionName.NORM,
            listOf(Metadatum("official long title", MetadatumType.OFFICIAL_LONG_TITLE)),
        )
    val documentTypeSection =
        MetadataSection(
            MetadataSectionName.DOCUMENT_TYPE,
            listOf(
                Metadatum("type name", MetadatumType.TYPE_NAME),
                Metadatum(NormCategory.BASE_NORM, MetadatumType.NORM_CATEGORY),
            ),
        )
    val normProviderSection1 =
        MetadataSection(
            MetadataSectionName.NORM_PROVIDER,
            listOf(
                Metadatum("entity", MetadatumType.ENTITY),
                Metadatum("deciding body", MetadatumType.DECIDING_BODY),
            ),
            order = 1)
    val normProviderSection2 =
        MetadataSection(
            MetadataSectionName.NORM_PROVIDER,
            listOf(
                Metadatum("entity2", MetadatumType.ENTITY),
                Metadatum("deciding body2", MetadatumType.DECIDING_BODY),
            ),
            order = 2)
    val result =
        HasAllMandatoryFields()
            .evaluate(
                listOf(
                    normSection, documentTypeSection, normProviderSection1, normProviderSection2))
    assertThat(result.isSatisfied).isTrue()
    assertThat(result.violations).isEmpty()
  }

  @Test
  fun `it is not satisfied if neither the norm nor the document type sections are not present`() {
    val anotherSection =
        MetadataSection(
            MetadataSectionName.PUBLICATION_DATE,
            listOf(Metadatum(LocalDate.now(), MetadatumType.DATE)),
        )
    val result = HasAllMandatoryFields().evaluate(listOf(anotherSection))
    assertThat(result.isNotSatisfied).isTrue()
    assertThat(result.violations).isNotEmpty().hasSize(5)
  }

  @Test
  fun `it is not satisfied if the norm section does not contain either official long title or the official short title`() {
    val normSection =
        MetadataSection(
            MetadataSectionName.NORM,
            listOf(Metadatum("official abbreviation", MetadatumType.OFFICIAL_ABBREVIATION)),
        )
    val documentTypeSection =
        MetadataSection(
            MetadataSectionName.DOCUMENT_TYPE,
            listOf(
                Metadatum("type name", MetadatumType.TYPE_NAME),
                Metadatum("template name", MetadatumType.TEMPLATE_NAME),
            ),
        )
    val normProviderSection1 =
        MetadataSection(
            MetadataSectionName.NORM_PROVIDER,
            listOf(
                Metadatum("entity", MetadatumType.ENTITY),
                Metadatum("decbody", MetadatumType.DECIDING_BODY),
            ),
            order = 1)
    val normProviderSection2 =
        MetadataSection(
            MetadataSectionName.NORM_PROVIDER,
            listOf(
                Metadatum("entity", MetadatumType.ENTITY),
            ),
            order = 2)
    val result =
        HasAllMandatoryFields()
            .evaluate(
                listOf(
                    normSection, documentTypeSection, normProviderSection1, normProviderSection2))
    assertThat(result.isNotSatisfied).isTrue()
    assertThat(result.violations)
        .isNotEmpty()
        .usingRecursiveComparison()
        .isEqualTo(
            listOf(
                violation("NORM", "OFFICIAL_LONG_TITLE"),
                violation("DOCUMENT_TYPE", "NORM_CATEGORY"),
                violation("NORM_PROVIDER", "DECIDING_BODY", 2),
            ))
  }
}

fun violation(
    sectionName: String,
    metadataType: String,
    order: Int? = null
): SpecificationViolation {
  return SpecificationViolation(
      sectionName + (if (order != null) "/$order/" else "/") + metadataType,
      "MANDATORY_FIELD_MISSING",
      "Norm does not contain the mandatory field $metadataType in section $sectionName" +
          if (order != null) " order nr. $order" else "",
  )
}
