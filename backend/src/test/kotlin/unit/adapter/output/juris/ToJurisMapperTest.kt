package unit.adapter.output.juris

import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.NormCategory
import de.bund.digitalservice.ris.norms.domain.value.ProofType
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import de.bund.digitalservice.ris.norms.framework.adapter.output.juris.mapDomainToData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.createRandomNorm
import java.time.LocalDate
import java.time.LocalTime
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection as Section

class ToJurisMapperTest {

    @Test
    fun `it correctly maps the domain to data`() {
        val norm = createRandomNorm().copy(
            metadataSections = listOf(
                Section(
                    MetadataSectionName.NORM,
                    listOf(
                        Metadatum("Aufhebung", MetadatumType.VALIDITY_RULE, order = 1),
                        Metadatum("Bedingungseintritt für bedingte Inkraftsetzung", MetadatumType.VALIDITY_RULE, order = 2),
                        Metadatum("minderjährig", MetadatumType.AGE_OF_MAJORITY_INDICATION, order = 1),
                        Metadatum("volljährig", MetadatumType.AGE_OF_MAJORITY_INDICATION, order = 2),
                        Metadatum("Zinsschranke", MetadatumType.DEFINITION, order = 1),
                        Metadatum("maßgebliches Einkommen (Abs 1)", MetadatumType.DEFINITION, order = 2),
                        Metadatum("1 BvR 2011/94", MetadatumType.REFERENCE_NUMBER, order = 1),
                        Metadatum("19 BvR 2019/23", MetadatumType.REFERENCE_NUMBER, order = 2),
                        Metadatum("BGBl I 2009, 1102", MetadatumType.UNOFFICIAL_REFERENCE, order = 1),
                        Metadatum("BGBl II 2022, 1351", MetadatumType.UNOFFICIAL_REFERENCE, order = 2),
                        Metadatum("Umsetzung EG-Recht", MetadatumType.KEYWORD, order = 1),
                        Metadatum("Mantelverordnung", MetadatumType.KEYWORD, order = 2),
                        Metadatum("BJNR0030A0023", MetadatumType.DIVERGENT_DOCUMENT_NUMBER, order = 1),
                        Metadatum("BJNR0040B0035", MetadatumType.DIVERGENT_DOCUMENT_NUMBER, order = 2),
                        Metadatum("FinVermStVtr", MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW, order = 1),
                        Metadatum("DesBerpStUar", MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW, order = 2),
                        Metadatum("Gebührenordnung für tierärztliche Leistungen", MetadatumType.UNOFFICIAL_LONG_TITLE, order = 1),
                        Metadatum("Das Geld für die Tiere", MetadatumType.UNOFFICIAL_LONG_TITLE, order = 2),
                        Metadatum("Tiergeld", MetadatumType.UNOFFICIAL_SHORT_TITLE, order = 1),
                        Metadatum("Animal money", MetadatumType.UNOFFICIAL_SHORT_TITLE, order = 2),
                        Metadatum("TG", MetadatumType.UNOFFICIAL_ABBREVIATION, order = 1),
                        Metadatum("AM", MetadatumType.UNOFFICIAL_ABBREVIATION, order = 2),
                    ),
                ),
                Section(
                    MetadataSectionName.SUBJECT_AREA,
                    listOf(
                        Metadatum("315-12", MetadatumType.SUBJECT_FNA),
                    ),
                    1,
                ),
                Section(
                    MetadataSectionName.SUBJECT_AREA,
                    listOf(
                        Metadatum("818-39", MetadatumType.SUBJECT_FNA),
                    ),
                    2,
                ),
                Section(
                    MetadataSectionName.SUBJECT_AREA,
                    listOf(
                        Metadatum("192-52", MetadatumType.SUBJECT_GESTA),
                    ),
                    3,
                ),
                Section(
                    MetadataSectionName.SUBJECT_AREA,
                    listOf(
                        Metadatum("562-01", MetadatumType.SUBJECT_GESTA),
                    ),
                    4,
                ),
                Section(
                    MetadataSectionName.LEAD,
                    listOf(
                        Metadatum("BMVBS", MetadatumType.LEAD_JURISDICTION),
                        Metadatum("RS III 2", MetadatumType.LEAD_UNIT),
                    ),
                    1,
                ),
                Section(
                    MetadataSectionName.LEAD,
                    listOf(
                        Metadatum("BMI", MetadatumType.LEAD_JURISDICTION),
                        Metadatum("Z I 2", MetadatumType.LEAD_UNIT),
                    ),
                    2,
                ),
                Section(
                    MetadataSectionName.PARTICIPATION,
                    listOf(
                        Metadatum("EZ", MetadatumType.PARTICIPATION_TYPE),
                        Metadatum("BR", MetadatumType.PARTICIPATION_INSTITUTION),
                    ),
                    1,
                ),
                Section(
                    MetadataSectionName.PARTICIPATION,
                    listOf(
                        Metadatum("RU", MetadatumType.PARTICIPATION_TYPE),
                        Metadatum("NT", MetadatumType.PARTICIPATION_INSTITUTION),
                    ),
                    2,
                ),
                Section(MetadataSectionName.CITATION_DATE, listOf(Metadatum("2002", MetadatumType.YEAR)), 1),
                Section(MetadataSectionName.CITATION_DATE, listOf(Metadatum(LocalDate.parse("2019-10-01"), MetadatumType.DATE)), 2),
                Section(
                    MetadataSectionName.AGE_INDICATION,
                    listOf(
                        Metadatum("Lebensjahr 28", MetadatumType.RANGE_START),
                        Metadatum("Lebensjahr 52", MetadatumType.RANGE_END),
                    ),
                    1,
                ),
                Section(
                    MetadataSectionName.AGE_INDICATION,
                    listOf(
                        Metadatum("Monat 10", MetadatumType.RANGE_START),
                        Metadatum("Monat 36", MetadatumType.RANGE_END),
                    ),
                    2,
                ),
                Section(
                    MetadataSectionName.OFFICIAL_REFERENCE,
                    emptyList(),
                    order = 1,
                    sections = listOf(
                        Section(
                            MetadataSectionName.PRINT_ANNOUNCEMENT,
                            listOf(
                                Metadatum("BGBl I", MetadatumType.ANNOUNCEMENT_GAZETTE),
                                Metadatum("2021", MetadatumType.YEAR),
                                Metadatum("3", MetadatumType.NUMBER),
                                Metadatum("56", MetadatumType.PAGE),
                                Metadatum("Additional info 1", MetadatumType.ADDITIONAL_INFO),
                                Metadatum("Explanation 1", MetadatumType.EXPLANATION),
                            ),
                        ),
                    ),
                ),
                Section(
                    MetadataSectionName.OFFICIAL_REFERENCE,
                    emptyList(),
                    order = 2,
                    sections = listOf(
                        Section(
                            MetadataSectionName.PRINT_ANNOUNCEMENT,
                            listOf(
                                Metadatum("BGBl II", MetadatumType.ANNOUNCEMENT_GAZETTE),
                                Metadatum("2019", MetadatumType.YEAR),
                                Metadatum("9", MetadatumType.NUMBER),
                                Metadatum("12", MetadatumType.PAGE),
                                Metadatum("Additional info 2", MetadatumType.ADDITIONAL_INFO),
                                Metadatum("Explanation 2", MetadatumType.EXPLANATION),
                            ),
                        ),
                    ),
                ),
                Section(
                    MetadataSectionName.OFFICIAL_REFERENCE,
                    emptyList(),
                    order = 3,
                    sections = listOf(
                        Section(
                            MetadataSectionName.DIGITAL_ANNOUNCEMENT,
                            listOf(
                                Metadatum("BGBl III", MetadatumType.ANNOUNCEMENT_MEDIUM),
                                Metadatum(LocalDate.parse("2011-12-03"), MetadatumType.DATE),
                                Metadatum("2011", MetadatumType.YEAR),
                                Metadatum("16", MetadatumType.EDITION),
                                Metadatum("52", MetadatumType.PAGE),
                                Metadatum("Area of publication 3", MetadatumType.AREA_OF_PUBLICATION),
                                Metadatum("Number of the publication 3", MetadatumType.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA),
                                Metadatum("Additional info 3", MetadatumType.ADDITIONAL_INFO),
                                Metadatum("Explanation 3", MetadatumType.EXPLANATION),
                            ),
                        ),
                    ),
                ),
                Section(
                    MetadataSectionName.OFFICIAL_REFERENCE,
                    emptyList(),
                    order = 4,
                    sections = listOf(
                        Section(
                            MetadataSectionName.DIGITAL_ANNOUNCEMENT,
                            listOf(
                                Metadatum("BGBl IV", MetadatumType.ANNOUNCEMENT_MEDIUM),
                                Metadatum(LocalDate.parse("2015-06-23"), MetadatumType.DATE),
                                Metadatum("2015", MetadatumType.YEAR),
                                Metadatum("65", MetadatumType.EDITION),
                                Metadatum("89", MetadatumType.PAGE),
                                Metadatum("Area of publication 4", MetadatumType.AREA_OF_PUBLICATION),
                                Metadatum("Number of the publication 4", MetadatumType.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA),
                                Metadatum("Additional info 4", MetadatumType.ADDITIONAL_INFO),
                                Metadatum("Explanation 4", MetadatumType.EXPLANATION),
                            ),
                        ),
                    ),
                ),
                Section(
                    MetadataSectionName.OFFICIAL_REFERENCE,
                    emptyList(),
                    order = 5,
                    sections = listOf(
                        Section(
                            MetadataSectionName.EU_ANNOUNCEMENT,
                            listOf(
                                Metadatum("BGBl I", MetadatumType.EU_GOVERNMENT_GAZETTE),
                                Metadatum("2021", MetadatumType.YEAR),
                                Metadatum("3", MetadatumType.SERIES),
                                Metadatum("56", MetadatumType.NUMBER),
                                Metadatum("89", MetadatumType.PAGE),
                                Metadatum("Additional info 1", MetadatumType.ADDITIONAL_INFO),
                                Metadatum("Explanation 1", MetadatumType.EXPLANATION),
                            ),
                        ),
                    ),
                ),
                Section(
                    MetadataSectionName.OFFICIAL_REFERENCE,
                    emptyList(),
                    order = 6,
                    sections = listOf(
                        Section(
                            MetadataSectionName.EU_ANNOUNCEMENT,
                            listOf(
                                Metadatum("BGBl II", MetadatumType.EU_GOVERNMENT_GAZETTE),
                                Metadatum("2019", MetadatumType.YEAR),
                                Metadatum("9", MetadatumType.SERIES),
                                Metadatum("12", MetadatumType.NUMBER),
                                Metadatum("89", MetadatumType.PAGE),
                                Metadatum("Additional info 2", MetadatumType.ADDITIONAL_INFO),
                                Metadatum("Explanation 2", MetadatumType.EXPLANATION),
                            ),
                        ),
                    ),
                ),
                Section(
                    MetadataSectionName.OFFICIAL_REFERENCE,
                    emptyList(),
                    order = 7,
                    sections = listOf(
                        Section(MetadataSectionName.OTHER_OFFICIAL_ANNOUNCEMENT, listOf(Metadatum("Other official reference 1", MetadatumType.OTHER_OFFICIAL_REFERENCE))),
                    ),
                ),

                Section(
                    MetadataSectionName.OFFICIAL_REFERENCE,
                    emptyList(),
                    order = 8,
                    sections = listOf(
                        Section(MetadataSectionName.OTHER_OFFICIAL_ANNOUNCEMENT, listOf(Metadatum("Other official reference 2", MetadatumType.OTHER_OFFICIAL_REFERENCE))),
                    ),
                ),

                Section(
                    MetadataSectionName.NORM_PROVIDER,
                    listOf(
                        Metadatum("DEU", MetadatumType.ENTITY),
                        Metadatum("BT", MetadatumType.DECIDING_BODY),
                        Metadatum(true, MetadatumType.RESOLUTION_MAJORITY),
                    ),
                    1,
                ),

                Section(
                    MetadataSectionName.NORM_PROVIDER,
                    listOf(
                        Metadatum("DEU", MetadatumType.ENTITY),
                        Metadatum("BMinI", MetadatumType.DECIDING_BODY),
                        Metadatum(false, MetadatumType.RESOLUTION_MAJORITY),
                    ),
                    2,
                ),

                Section(
                    MetadataSectionName.DOCUMENT_TYPE,
                    listOf(
                        Metadatum("RV", MetadatumType.TYPE_NAME, order = 1),
                        Metadatum(NormCategory.BASE_NORM, MetadatumType.NORM_CATEGORY, order = 1),
                        Metadatum(NormCategory.TRANSITIONAL_NORM, MetadatumType.NORM_CATEGORY, order = 2),
                        Metadatum("template 1", MetadatumType.TEMPLATE_NAME, order = 1),
                        Metadatum("template 2", MetadatumType.TEMPLATE_NAME, order = 2),
                    ),
                ),
                Section(
                    MetadataSectionName.CATEGORIZED_REFERENCE,
                    listOf(
                        Metadatum("&A 31 &B Art 1 Nr 4 Buchst a &E HGB &E3 § 246 &E6 Abs 1", MetadatumType.TEXT),
                    ),
                    1,
                ),
                Section(
                    MetadataSectionName.CATEGORIZED_REFERENCE,
                    listOf(
                        Metadatum("&A 32 &B Art 1 Nr 4 Buchst b &E HGB &E3 § 246 &E6 Abs 2 S 2 und 3", MetadatumType.TEXT),
                    ),
                    2,
                ),
                Section(
                    MetadataSectionName.ENTRY_INTO_FORCE,
                    listOf(
                        Metadatum(LocalDate.parse("2018-12-21"), MetadatumType.DATE),
                    ),
                ),
                Section(
                    MetadataSectionName.PRINCIPLE_ENTRY_INTO_FORCE,
                    listOf(
                        Metadatum(LocalDate.parse("2020-03-15"), MetadatumType.DATE),
                    ),
                ),
                Section(
                    MetadataSectionName.EXPIRATION,
                    listOf(
                        Metadatum(UndefinedDate.UNDEFINED_UNKNOWN, MetadatumType.UNDEFINED_DATE),
                    ),
                ),
                Section(
                    MetadataSectionName.PRINCIPLE_EXPIRATION,
                    listOf(
                        Metadatum(UndefinedDate.UNDEFINED_UNKNOWN, MetadatumType.UNDEFINED_DATE),
                    ),
                ),

                Section(
                    MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE,
                    emptyList(),
                    order = 1,
                    sections = listOf(
                        Section(
                            MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED,
                            listOf(
                                Metadatum(LocalDate.parse("2014-11-11"), MetadatumType.DATE),
                                Metadatum(NormCategory.AMENDMENT_NORM, MetadatumType.NORM_CATEGORY),
                            ),
                        ),
                    ),
                ),

                Section(
                    MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE,
                    emptyList(),
                    order = 2,
                    sections = listOf(
                        Section(
                            MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED,
                            listOf(
                                Metadatum(LocalDate.parse("2010-06-15"), MetadatumType.DATE),
                                Metadatum(NormCategory.BASE_NORM, MetadatumType.NORM_CATEGORY),
                                Metadatum(NormCategory.TRANSITIONAL_NORM, MetadatumType.NORM_CATEGORY),
                            ),
                        ),
                    ),
                ),

                Section(
                    MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE,
                    emptyList(),
                    order = 3,
                    sections = listOf(
                        Section(
                            MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED,
                            listOf(
                                Metadatum(UndefinedDate.UNDEFINED_FUTURE, MetadatumType.UNDEFINED_DATE),
                                Metadatum(NormCategory.TRANSITIONAL_NORM, MetadatumType.NORM_CATEGORY),
                            ),
                        ),
                    ),
                ),

                Section(
                    MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE,
                    emptyList(),
                    order = 4,
                    sections = listOf(
                        Section(
                            MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED,
                            listOf(
                                Metadatum(UndefinedDate.UNDEFINED_NOT_PRESENT, MetadatumType.UNDEFINED_DATE),
                                Metadatum(NormCategory.AMENDMENT_NORM, MetadatumType.NORM_CATEGORY),
                            ),
                        ),
                    ),
                ),

                Section(
                    MetadataSectionName.DIVERGENT_EXPIRATION,
                    emptyList(),
                    order = 1,
                    sections = listOf(
                        Section(
                            MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED,
                            listOf(
                                Metadatum(LocalDate.parse("2011-11-09"), MetadatumType.DATE),
                                Metadatum(NormCategory.TRANSITIONAL_NORM, MetadatumType.NORM_CATEGORY),
                            ),
                        ),
                    ),
                ),

                Section(
                    MetadataSectionName.DIVERGENT_EXPIRATION,
                    emptyList(),
                    order = 2,
                    sections = listOf(
                        Section(
                            MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED,
                            listOf(
                                Metadatum(LocalDate.parse("2008-05-29"), MetadatumType.DATE),
                                Metadatum(NormCategory.BASE_NORM, MetadatumType.NORM_CATEGORY),
                            ),
                        ),
                    ),
                ),

                Section(
                    MetadataSectionName.DIVERGENT_EXPIRATION,
                    emptyList(),
                    order = 3,
                    sections = listOf(
                        Section(
                            MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED,
                            listOf(
                                Metadatum(UndefinedDate.UNDEFINED_FUTURE, MetadatumType.UNDEFINED_DATE),
                                Metadatum(NormCategory.BASE_NORM, MetadatumType.NORM_CATEGORY),
                                Metadatum(NormCategory.AMENDMENT_NORM, MetadatumType.NORM_CATEGORY),
                            ),
                        ),
                    ),
                ),

                Section(
                    MetadataSectionName.STATUS_INDICATION,
                    emptyList(),
                    order = 1,
                    sections = listOf(
                        Section(
                            MetadataSectionName.STATUS,
                            listOf(
                                Metadatum("statusNote", MetadatumType.NOTE),
                                Metadatum("statusDescription", MetadatumType.DESCRIPTION),
                                Metadatum(LocalDate.parse("2010-01-01"), MetadatumType.DATE),
                                Metadatum("statusReference1", MetadatumType.REFERENCE, 1),
                                Metadatum("statusReference2", MetadatumType.REFERENCE, 2),
                            ),
                        ),
                    ),
                ),
                Section(
                    MetadataSectionName.STATUS_INDICATION,
                    emptyList(),
                    order = 2,
                    sections = listOf(
                        Section(
                            MetadataSectionName.REISSUE,
                            listOf(
                                Metadatum("reissueNote", MetadatumType.NOTE),
                                Metadatum("reissueArticle", MetadatumType.ARTICLE),
                                Metadatum(LocalDate.parse("2012-02-02"), MetadatumType.DATE),
                                Metadatum("reissueReference", MetadatumType.REFERENCE),
                            ),
                        ),
                    ),
                ),

                Section(
                    MetadataSectionName.STATUS_INDICATION,
                    emptyList(),
                    order = 3,
                    sections = listOf(
                        Section(
                            MetadataSectionName.REPEAL,
                            listOf(
                                Metadatum("repealReference1", MetadatumType.TEXT),
                            ),
                        ),
                    ),
                ),
                Section(
                    MetadataSectionName.STATUS_INDICATION,
                    emptyList(),
                    order = 4,
                    sections = listOf(
                        Section(
                            MetadataSectionName.OTHER_STATUS,
                            listOf(
                                Metadatum("otherStatusNote", MetadatumType.NOTE),
                            ),
                        ),
                    ),
                ),
                Section(
                    MetadataSectionName.STATUS_INDICATION,
                    emptyList(),
                    order = 5,
                    sections = listOf(
                        Section(
                            MetadataSectionName.REPEAL,
                            listOf(
                                Metadatum("repealReference2", MetadatumType.TEXT),
                            ),
                        ),
                    ),
                ),
                Section(
                    MetadataSectionName.FOOTNOTES,
                    listOf(
                        Metadatum("reference1", MetadatumType.FOOTNOTE_REFERENCE, order = 1),
                        Metadatum("footnoteChangeA", MetadatumType.FOOTNOTE_CHANGE, order = 2),
                        Metadatum("footnoteComment1", MetadatumType.FOOTNOTE_COMMENT, order = 3),
                        Metadatum("footnoteChangeB", MetadatumType.FOOTNOTE_CHANGE, order = 4),
                        Metadatum("footnoteDecision1", MetadatumType.FOOTNOTE_DECISION, order = 5),
                    ),
                ),
                Section(
                    MetadataSectionName.DOCUMENT_STATUS_SECTION,
                    emptyList(),
                    order = 1,
                    sections = listOf(
                        Section(
                            MetadataSectionName.DOCUMENT_TEXT_PROOF,
                            listOf(
                                Metadatum(ProofType.TEXT_PROOF_FROM, MetadatumType.PROOF_TYPE),
                                Metadatum("26.10.2001", MetadatumType.TEXT),
                            ),
                        ),
                    ),
                ),
                Section(
                    MetadataSectionName.DOCUMENT_STATUS_SECTION,
                    emptyList(),
                    order = 1,
                    sections = listOf(
                        Section(
                            MetadataSectionName.DOCUMENT_STATUS,
                            listOf(
                                Metadatum("documentStatusNote", MetadatumType.WORK_NOTE),
                                Metadatum("documentStatusDescription", MetadatumType.DESCRIPTION),
                                Metadatum(LocalDate.parse("2010-03-04"), MetadatumType.DATE),
                            ),
                        ),
                    ),
                ),
                Section(
                    MetadataSectionName.ANNOUNCEMENT_DATE,
                    listOf(
                        Metadatum(LocalDate.parse("2022-01-07"), MetadatumType.DATE),
                    ),
                ),

            ),

        )

        val normData = mapDomainToData(norm)

        assertThat(normData.validityRuleList).hasSize(2)
        assertThat(normData.validityRuleList[0]).isEqualTo("Aufhebung")
        assertThat(normData.validityRuleList[1]).isEqualTo("Bedingungseintritt für bedingte Inkraftsetzung")

        assertThat(normData.ageOfMajorityIndicationList).hasSize(2)
        assertThat(normData.ageOfMajorityIndicationList[0]).isEqualTo("minderjährig")
        assertThat(normData.ageOfMajorityIndicationList[1]).isEqualTo("volljährig")

        assertThat(normData.definitionList).hasSize(2)
        assertThat(normData.definitionList[0]).isEqualTo("Zinsschranke")
        assertThat(normData.definitionList[1]).isEqualTo("maßgebliches Einkommen (Abs 1)")

        assertThat(normData.referenceNumberList).hasSize(2)
        assertThat(normData.referenceNumberList[0]).isEqualTo("1 BvR 2011/94")
        assertThat(normData.referenceNumberList[1]).isEqualTo("19 BvR 2019/23")

        assertThat(normData.unofficialReferenceList).hasSize(2)
        assertThat(normData.unofficialReferenceList[0]).isEqualTo("BGBl I 2009, 1102")
        assertThat(normData.unofficialReferenceList[1]).isEqualTo("BGBl II 2022, 1351")

        assertThat(normData.frameKeywordList).hasSize(2)
        assertThat(normData.frameKeywordList[0]).isEqualTo("Umsetzung EG-Recht")
        assertThat(normData.frameKeywordList[1]).isEqualTo("Mantelverordnung")

        assertThat(normData.divergentDocumentNumber).isEqualTo("BJNR0030A0023")

        assertThat(normData.risAbbreviationInternationalLawList).hasSize(2)
        assertThat(normData.risAbbreviationInternationalLawList[0]).isEqualTo("FinVermStVtr")
        assertThat(normData.risAbbreviationInternationalLawList[1]).isEqualTo("DesBerpStUar")

        assertThat(normData.unofficialLongTitleList).hasSize(2)
        assertThat(normData.unofficialLongTitleList[0]).isEqualTo("Gebührenordnung für tierärztliche Leistungen")
        assertThat(normData.unofficialLongTitleList[1]).isEqualTo("Das Geld für die Tiere")

        assertThat(normData.unofficialShortTitleList).hasSize(2)
        assertThat(normData.unofficialShortTitleList[0]).isEqualTo("Tiergeld")
        assertThat(normData.unofficialShortTitleList[1]).isEqualTo("Animal money")

        assertThat(normData.unofficialAbbreviationList).hasSize(2)
        assertThat(normData.unofficialAbbreviationList[0]).isEqualTo("TG")
        assertThat(normData.unofficialAbbreviationList[1]).isEqualTo("AM")

        assertThat(normData.subjectAreaList).hasSize(4)
        assertThat(normData.subjectAreaList[0].fna).isEqualTo("315-12")
        assertThat(normData.subjectAreaList[1].fna).isEqualTo("818-39")
        assertThat(normData.subjectAreaList[2].gesta).isEqualTo("192-52")
        assertThat(normData.subjectAreaList[3].gesta).isEqualTo("562-01")

        assertThat(normData.leadList).hasSize(2)
        assertThat(normData.leadList[0].jurisdiction).isEqualTo("BMVBS")
        assertThat(normData.leadList[0].unit).isEqualTo("RS III 2")
        assertThat(normData.leadList[1].jurisdiction).isEqualTo("BMI")
        assertThat(normData.leadList[1].unit).isEqualTo("Z I 2")

        assertThat(normData.participationList).hasSize(2)
        assertThat(normData.participationList[0].type).isEqualTo("EZ")
        assertThat(normData.participationList[0].institution).isEqualTo("BR")
        assertThat(normData.participationList[1].type).isEqualTo("RU")
        assertThat(normData.participationList[1].institution).isEqualTo("NT")

        assertThat(normData.citationDateList).hasSize(2)
        assertThat(normData.citationDateList[0]).isEqualTo("2002")
        assertThat(normData.citationDateList[1]).isEqualTo("2019-10-01")

        assertThat(normData.ageIndicationStartList).hasSize(2)
        assertThat(normData.ageIndicationStartList[0]).isEqualTo("Lebensjahr 28")
        assertThat(normData.ageIndicationStartList[1]).isEqualTo("Monat 10")

        assertThat(normData.printAnnouncementList).hasSize(2)
        assertThat(normData.printAnnouncementList[0].gazette).isEqualTo("BGBl I")
        assertThat(normData.printAnnouncementList[0].year).isEqualTo("2021")
        assertThat(normData.printAnnouncementList[0].page).isEqualTo("56")
        assertThat(normData.printAnnouncementList[1].gazette).isEqualTo("BGBl II")
        assertThat(normData.printAnnouncementList[1].year).isEqualTo("2019")
        assertThat(normData.printAnnouncementList[1].page).isEqualTo("12")

        assertThat(normData.digitalAnnouncementList).hasSize(2)
        assertThat(normData.digitalAnnouncementList[0].medium).isEqualTo("BGBl III")
        assertThat(normData.digitalAnnouncementList[0].year).isEqualTo("2011")
        assertThat(normData.digitalAnnouncementList[0].number).isEqualTo("16")
        assertThat(normData.digitalAnnouncementList[1].medium).isEqualTo("BGBl IV")
        assertThat(normData.digitalAnnouncementList[1].year).isEqualTo("2015")
        assertThat(normData.digitalAnnouncementList[1].number).isEqualTo("65")

        assertThat(normData.normProviderList).hasSize(2)
        assertThat(normData.normProviderList[0].entity).isEqualTo("DEU")
        assertThat(normData.normProviderList[0].decidingBody).isEqualTo("BT")
        assertThat(normData.normProviderList[0].isResolutionMajority).isEqualTo(true)
        assertThat(normData.normProviderList[1].entity).isEqualTo("DEU")
        assertThat(normData.normProviderList[1].decidingBody).isEqualTo("BMinI")
        assertThat(normData.normProviderList[1].isResolutionMajority).isEqualTo(false)

        assertThat(normData.documentType).isNotNull
        assertThat(normData.documentType?.name).isEqualTo("RV")
        assertThat(normData.documentType?.categories).hasSize(2)
        assertThat(normData.documentType?.categories!![0]).isEqualTo("SN")
        assertThat(normData.documentType?.categories!![1]).isEqualTo("ÜN")
        assertThat(normData.documentType?.templateNames).hasSize(2)
        assertThat(normData.documentType?.templateNames!![0]).isEqualTo("template 1")
        assertThat(normData.documentType?.templateNames!![1]).isEqualTo("template 2")

        assertThat(normData.categorizedReferences).hasSize(2)
        assertThat(normData.categorizedReferences[0].text).isEqualTo("&A 31 &B Art 1 Nr 4 Buchst a &E HGB &E3 § 246 &E6 Abs 1")
        assertThat(normData.categorizedReferences[1].text).isEqualTo("&A 32 &B Art 1 Nr 4 Buchst b &E HGB &E3 § 246 &E6 Abs 2 S 2 und 3")

        assertThat(normData.entryIntoForceDate).isEqualTo("2018-12-21")
        assertThat(normData.entryIntoForceDateState).isNull()
        assertThat(normData.principleEntryIntoForceDate).isNull()
        assertThat(normData.principleEntryIntoForceDateState).isNull()
        assertThat(normData.expirationDate).isNull()
        assertThat(normData.expirationDateState).isEqualTo("UNDEFINED_UNKNOWN")
        assertThat(normData.principleExpirationDate).isNull()
        assertThat(normData.principleExpirationDateState).isNull()

        assertThat(normData.divergentEntryIntoForceList).hasSize(4)
        assertThat(normData.divergentEntryIntoForceList[0].date).isEqualTo("2014-11-11")
        assertThat(normData.divergentEntryIntoForceList[0].state).isNull()
        assertThat(normData.divergentEntryIntoForceList[0].normCategory).isEqualTo("ÄN")
        assertThat(normData.divergentEntryIntoForceList[1].date).isEqualTo("2010-06-15")
        assertThat(normData.divergentEntryIntoForceList[1].state).isNull()
        assertThat(normData.divergentEntryIntoForceList[1].normCategory).isEqualTo("SN")
        assertThat(normData.divergentEntryIntoForceList[2].date).isNull()
        assertThat(normData.divergentEntryIntoForceList[2].state).isEqualTo("UNDEFINED_FUTURE")
        assertThat(normData.divergentEntryIntoForceList[2].normCategory).isEqualTo("ÜN")
        assertThat(normData.divergentEntryIntoForceList[3].date).isNull()
        assertThat(normData.divergentEntryIntoForceList[3].state).isEqualTo("UNDEFINED_NOT_PRESENT")
        assertThat(normData.divergentEntryIntoForceList[3].normCategory).isEqualTo("ÄN")

        assertThat(normData.divergentExpirationsList).hasSize(3)
        assertThat(normData.divergentExpirationsList[0].date).isEqualTo("2011-11-09")
        assertThat(normData.divergentExpirationsList[0].state).isNull()
        assertThat(normData.divergentExpirationsList[0].normCategory).isEqualTo("ÜN")
        assertThat(normData.divergentExpirationsList[1].date).isEqualTo("2008-05-29")
        assertThat(normData.divergentExpirationsList[1].state).isNull()
        assertThat(normData.divergentExpirationsList[1].normCategory).isEqualTo("SN")
        assertThat(normData.divergentExpirationsList[2].date).isNull()
        assertThat(normData.divergentExpirationsList[2].state).isEqualTo("UNDEFINED_FUTURE")
        assertThat(normData.divergentExpirationsList[2].normCategory).isEqualTo("SN")

        assertThat(normData.statusList).hasSize(1)
        assertThat(normData.statusList[0].statusNote).isEqualTo("statusNote")
        assertThat(normData.statusList[0].statusDescription).isEqualTo("statusDescription")
        assertThat(normData.statusList[0].statusDate).isEqualTo("2010-01-01")
        assertThat(normData.statusList[0].statusReference).isEqualTo("statusReference1, statusReference2")
        assertThat(normData.reissueList).hasSize(1)
        assertThat(normData.reissueList[0].reissueNote).isEqualTo("reissueNote")
        assertThat(normData.reissueList[0].reissueArticle).isEqualTo("reissueArticle")
        assertThat(normData.reissueList[0].reissueDate).isEqualTo("2012-02-02")
        assertThat(normData.reissueList[0].reissueReference).isEqualTo("reissueReference")
        assertThat(normData.repealList).hasSize(2)
        assertThat(normData.repealList.containsAll(listOf("repealReference1", "repealReference2"))).isTrue()
        assertThat(normData.otherStatusList).hasSize(1)
        assertThat(normData.otherStatusList[0]).isEqualTo("otherStatusNote")

        assertThat(normData.footnotes).hasSize(1)
        assertThat(normData.footnotes[0].reference).isEqualTo("reference1")
        assertThat(normData.footnotes[0].footnoteChange[0].first).isEqualTo(2)
        assertThat(normData.footnotes[0].footnoteChange[0].second).isEqualTo("footnoteChangeA")
        assertThat(normData.footnotes[0].footnoteComment[0].first).isEqualTo(3)
        assertThat(normData.footnotes[0].footnoteComment[0].second).isEqualTo("footnoteComment1")
        assertThat(normData.footnotes[0].footnoteChange[1].first).isEqualTo(4)
        assertThat(normData.footnotes[0].footnoteChange[1].second).isEqualTo("footnoteChangeB")
        assertThat(normData.footnotes[0].footnoteDecision[0].first).isEqualTo(5)
        assertThat(normData.footnotes[0].footnoteDecision[0].second).isEqualTo("footnoteDecision1")

        assertThat(normData.documentTextProof).isEqualTo("Textnachweis ab: 26.10.2001")
        assertThat(normData.documentStatus).hasSize(1)
        assertThat(normData.documentStatus[0].documentStatusWorkNote[0]).isEqualTo("documentStatusNote")
        assertThat(normData.documentStatus[0].documentStatusDescription).isEqualTo("documentStatusDescription")
        assertThat(normData.documentStatus[0].documentStatusDateYear).isEqualTo("2010-03-04")

        assertThat(normData.announcementDate).isEqualTo("2022-01-07")
    }

    @Test
    fun `it correctly maps the announcement date if it is a year`() {
        val norm = createRandomNorm().copy(
            metadataSections = listOf(
                Section(
                    MetadataSectionName.ANNOUNCEMENT_DATE,
                    listOf(Metadatum("2022", MetadatumType.YEAR)),
                ),
            ),
        )

        val normData = mapDomainToData(norm)

        assertThat(normData.announcementDate).isEqualTo("2022")
    }

    @Test
    fun `it correctly maps the announcement date if it is a date but without time`() {
        val norm = createRandomNorm().copy(
            metadataSections = listOf(
                Section(
                    MetadataSectionName.ANNOUNCEMENT_DATE,
                    listOf(Metadatum(LocalDate.parse("2022-01-07"), MetadatumType.DATE)),
                ),
            ),
        )

        val normData = mapDomainToData(norm)

        assertThat(normData.announcementDate).isEqualTo("2022-01-07")
    }

    @Test
    fun `it correctly maps the announcement date if it is a date and has a time`() {
        val norm = createRandomNorm().copy(
            metadataSections = listOf(
                Section(
                    MetadataSectionName.ANNOUNCEMENT_DATE,
                    listOf(
                        Metadatum(LocalDate.parse("2022-01-07"), MetadatumType.DATE),
                        Metadatum(LocalTime.parse("08:56"), MetadatumType.TIME),
                    ),
                ),
            ),
        )

        val normData = mapDomainToData(norm)

        assertThat(normData.announcementDate).isEqualTo("2022-01-07 08:56")
    }
}
