import { expect, test } from "@playwright/test"
import { Norm, NormCategory, UndefinedDate } from "../../../src/domain/Norm"
import { importNormViaApi, loadJurisTestFile } from "./e2e-utils"
import { normData } from "./testdata/norm_basic"
import { FieldType, MetadataInputSection } from "./utilities"

type MyFixtures = {
  normData: NormData
  guid: string
}

// This helps to get clean and simple hover types in the IDE.
type CleanTypeWrapper<Type> = unknown & {
  [Property in keyof Type]: Type[Property]
}

type UnionOmit<Type, KeyToOmit extends PropertyKey> = Type extends unknown
  ? Omit<Type, KeyToOmit>
  : never

type NullUnionOmit<Type, KeyToOmit extends PropertyKey> = null extends Type
  ? UnionOmit<NonNullable<Type>, KeyToOmit>
  : UnionOmit<Type, KeyToOmit>

type RecursiveOmitHelper<Type, KeyToOmit extends PropertyKey> = {
  [Property in keyof Type]: CleanTypeWrapper<
    RecursiveOmit<Type[Property], KeyToOmit>
  >
}

/**
 * Used to recursively omit a property via its key from a nested object type.
 * While `Omit<Type, Key>` only removes the property from the first level, this
 * does it deeply nested.
 * The challenge within the helper types is to address the problem of nullable
 * type unions and proper tool tips within development environments.
 *
 * @example
 * ```ts
 * type WithProperty = {
 *   a: string, b: number, c: {
 *     a: string, d: boolean, e: {
 *       a: string, f: string | null
 *     }
 *   }
 * }
 * type WithoutPropertyA = RecursiveOmit<WithProperty, 'a'>
 * // => // { b: number, c: { d: boolean, e: { f: string | null } } }
 * ```
 */
type RecursiveOmit<Type, KeyToOmit extends PropertyKey> = Type extends {
  [Property in KeyToOmit]: unknown
}
  ? NullUnionOmit<RecursiveOmitHelper<Type, KeyToOmit>, KeyToOmit>
  : RecursiveOmitHelper<Type, KeyToOmit>

function undefinedDateToDropdownEntry(
  unit?: UndefinedDate
): string | undefined {
  switch (unit) {
    case UndefinedDate.UNDEFINED_UNKNOWN:
      return "unbestimmt (unbekannt)"
    case UndefinedDate.UNDEFINED_FUTURE:
      return "unbestimmt (zukünftig)"
    case UndefinedDate.UNDEFINED_NOT_PRESENT:
      return "nicht vorhanden"
    default:
      return undefined
  }
}

export type NormData = RecursiveOmit<Norm, "guid"> & {
  jurisZipFileName: string
}

export const testWithImportedNorm = test.extend<MyFixtures>({
  normData,
  guid: async ({ normData, request }, use) => {
    const fileName = normData.jurisZipFileName

    expect(fileName).toBeTruthy()

    const { fileContent } = await loadJurisTestFile(request, fileName)
    const { guid } = await importNormViaApi(request, fileContent, fileName)

    await use(guid)
  },
})

export function getNormBySections(norm: NormData): MetadataInputSection[] {
  return [
    {
      heading: "Allgemeine Angaben",
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.TEXT,
          id: "officialLongTitle",
          label: "Amtliche Langüberschrift",
          value: norm.officialLongTitle,
        },
      ],
    },
    {
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.TEXT,
          id: "risAbbreviation",
          label: "Juris-Abkürzung",
          value: norm.risAbbreviation,
        },
      ],
    },
    {
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.CHIPS,
          id: "risAbbreviationInternationalLaw",
          label: "Juris-Abkürzung für völkerrechtliche Vereinbarungen",
          value:
            norm.metadataSections?.NORM?.[0].RIS_ABBREVIATION_INTERNATIONAL_LAW,
        },
      ],
    },
    {
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.TEXT,
          id: "documentNumber",
          label: "Dokumentnummer",
          value: norm.documentNumber,
        },
      ],
    },
    {
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.CHIPS,
          id: "divergentDocumentNumbers",
          label: "Abweichende Dokumentnummer",
          value: norm.metadataSections?.NORM?.[0].DIVERGENT_DOCUMENT_NUMBER,
        },
      ],
    },
    {
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.TEXT,
          id: "documentCategory",
          label: "Dokumentart",
          value: norm.documentCategory,
        },
      ],
    },
    {
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.CHIPS,
          id: "frameKeywords",
          label: "Schlagwörter im Rahmenelement",
          value: norm.metadataSections?.NORM?.[0].KEYWORD,
        },
      ],
    },
    {
      heading: "Dokumenttyp",
      id: "documentTypes",
      fields: [
        {
          type: FieldType.TEXT,
          id: "documentTypeName",
          label: "Typbezeichnung",
          values: norm.metadataSections?.DOCUMENT_TYPE?.map(
            (section) => section?.TYPE_NAME?.[0]
          ),
        },
        {
          type: FieldType.CHECKBOX,
          id: NormCategory.AMENDMENT_NORM,
          label: "Änderungsnorm",
          values: norm.metadataSections?.DOCUMENT_TYPE?.map(
            (section) =>
              !!section?.NORM_CATEGORY?.find(
                (category) => category == NormCategory.AMENDMENT_NORM
              )
          ),
        },
        {
          type: FieldType.CHECKBOX,
          id: NormCategory.BASE_NORM,
          label: "Stammnorm",
          values: norm.metadataSections?.DOCUMENT_TYPE?.map(
            (section) =>
              !!section?.NORM_CATEGORY?.find(
                (category) => category == NormCategory.BASE_NORM
              )
          ),
        },
        {
          type: FieldType.CHECKBOX,
          id: NormCategory.TRANSITIONAL_NORM,
          label: "Übergangsnorm",
          values: norm.metadataSections?.DOCUMENT_TYPE?.map(
            (section) =>
              !!section?.NORM_CATEGORY?.find(
                (category) => category == NormCategory.TRANSITIONAL_NORM
              )
          ),
        },
        {
          type: FieldType.CHIPS,
          id: "documentTemplateName",
          label: "Bezeichnung gemäß Vorlage",
          values: norm.metadataSections?.DOCUMENT_TYPE?.map(
            (section) => section?.TEMPLATE_NAME
          ),
        },
      ],
    },
    {
      heading: "Normgeber",
      isRepeatedSection: true,
      id: "normProviders",
      fields: [
        {
          type: FieldType.TEXT,
          id: "normProviderEntity",
          label:
            "Staat, Land, Stadt, Landkreis oder juristische Person, deren Hoheitsgewalt oder Rechtsmacht die Norm trägt",
          values: norm.metadataSections?.NORM_PROVIDER?.map(
            (section) => section?.ENTITY?.[0]
          ),
        },
        {
          type: FieldType.TEXT,
          id: "normProviderDecidingBody",
          label: "Beschließendes Organ",
          values: norm.metadataSections?.NORM_PROVIDER?.map(
            (section) => section?.DECIDING_BODY?.[0]
          ),
        },
        {
          type: FieldType.CHECKBOX,
          id: "normProviderIsResolutionMajority",
          label: "Beschlussfassung mit qualifizierter Mehrheit",
          values: norm.metadataSections?.NORM_PROVIDER?.map(
            (section) => section?.RESOLUTION_MAJORITY?.[0]
          ),
        },
      ],
    },
    {
      heading: "Mitwirkende Organe",
      id: "participatingInstitutions",
      isRepeatedSection: true,
      fields: [
        {
          type: FieldType.TEXT,
          id: "participationType",
          label: "Art der Mitwirkung",
          values: norm.metadataSections?.PARTICIPATION?.map(
            (section) => section?.PARTICIPATION_TYPE?.[0]
          ),
        },
        {
          type: FieldType.TEXT,
          id: "participationInstitution",
          label: "Mitwirkendes Organ",
          values: norm.metadataSections?.PARTICIPATION?.map(
            (section) => section?.PARTICIPATION_INSTITUTION?.[0]
          ),
        },
      ],
    },
    {
      heading: "Federführung",
      isRepeatedSection: true,
      id: "leads",
      fields: [
        {
          type: FieldType.TEXT,
          id: "leadJurisdiction",
          label: "Ressort",
          values: norm.metadataSections?.LEAD?.map(
            (section) => section?.LEAD_JURISDICTION?.[0]
          ),
        },
        {
          type: FieldType.TEXT,
          id: "leadUnit",
          label: "Organisationseinheit",
          values: norm.metadataSections?.LEAD?.map(
            (section) => section?.LEAD_UNIT?.[0]
          ),
        },
      ],
    },
    {
      heading: "Sachgebiet",
      isRepeatedSection: true,
      id: "subjectAreas",
      fields: [
        {
          type: FieldType.TEXT,
          id: "subjectFna",
          label: "FNA-Nummer",
          values: norm.metadataSections?.SUBJECT_AREA?.map(
            (section) => section?.SUBJECT_FNA?.[0]
          ),
        },
        {
          type: FieldType.TEXT,
          id: "subjectPreviousFna",
          label: "Frühere FNA-Nummer",
          values: norm.metadataSections?.SUBJECT_AREA?.map(
            (section) => section?.SUBJECT_PREVIOUS_FNA?.[0]
          ),
        },
        {
          type: FieldType.TEXT,
          id: "subjectGesta",
          label: "GESTA-Nummer",
          values: norm.metadataSections?.SUBJECT_AREA?.map(
            (section) => section?.SUBJECT_GESTA?.[0]
          ),
        },
        {
          type: FieldType.TEXT,
          id: "subjectBgb3",
          label: "Bundesgesetzblatt Teil III",
          values: norm.metadataSections?.SUBJECT_AREA?.map(
            (section) => section?.SUBJECT_BGB_3?.[0]
          ),
        },
      ],
    },
    {
      heading: "Überschriften und Abkürzungen",
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.TEXT,
          id: "officialShortTitle",
          label: "Amtliche Kurzüberschrift",
          value: norm.officialShortTitle,
        },
      ],
    },
    {
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.TEXT,
          id: "officialAbbreviation",
          label: "Amtliche Buchstabenabkürzung",
          value: norm.officialAbbreviation,
        },
      ],
    },
    {
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.CHIPS,
          id: "unofficialLongTitles",
          label: "Nichtamtliche Langüberschrift",
          value: norm.metadataSections?.NORM?.[0].UNOFFICIAL_LONG_TITLE,
        },
      ],
    },
    {
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.CHIPS,
          id: "unofficialShortTitles",
          label: "Nichtamtliche Kurzüberschrift",
          value: norm.metadataSections?.NORM?.[0].UNOFFICIAL_SHORT_TITLE,
        },
      ],
    },
    {
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.CHIPS,
          id: "unofficialAbbreviations",
          label: "Nichtamtliche Buchstabenabkürzung",
          value: norm.metadataSections?.NORM?.[0].UNOFFICIAL_ABBREVIATION,
        },
      ],
    },
    {
      heading: "Abweichendes Inkrafttretedatum",
      isRepeatedSection: true,
      id: "divergentEntryIntoForces",
      fields: [
        {
          type: FieldType.RADIO,
          id: "divergentEntryIntoForceDefinedSelection",
          label: "bestimmt",
          values: norm.metadataSections?.DIVERGENT_ENTRY_INTO_FORCE?.map(
            (section) => !!section?.DIVERGENT_ENTRY_INTO_FORCE_DEFINED
          ),
        },
        {
          type: FieldType.TEXT,
          id: "divergentEntryIntoForceDefinedDate",
          label: "Bestimmtes grundsätzliches Inkrafttretedatum Date Input",
          values: norm.metadataSections?.DIVERGENT_ENTRY_INTO_FORCE?.map(
            (section) => section?.DIVERGENT_ENTRY_INTO_FORCE_DEFINED?.[0]
          ).map((section) => section?.DATE?.[0]),
        },
        {
          type: FieldType.CHECKBOX,
          id: NormCategory.AMENDMENT_NORM,
          label: "Änderungsnorm",
          values:
            norm.metadataSections?.DIVERGENT_ENTRY_INTO_FORCE_DEFINED?.map(
              (section) =>
                !!section?.NORM_CATEGORY?.find(
                  (category) => category == NormCategory.AMENDMENT_NORM
                )
            ),
        },
        {
          type: FieldType.CHECKBOX,
          id: NormCategory.BASE_NORM,
          label: "Stammnorm",
          values:
            norm.metadataSections?.DIVERGENT_ENTRY_INTO_FORCE_DEFINED?.map(
              (section) =>
                !!section?.NORM_CATEGORY?.find(
                  (category) => category == NormCategory.BASE_NORM
                )
            ),
        },
        {
          type: FieldType.CHECKBOX,
          id: NormCategory.TRANSITIONAL_NORM,
          label: "Übergangsnorm",
          values:
            norm.metadataSections?.DIVERGENT_ENTRY_INTO_FORCE_DEFINED?.map(
              (section) =>
                !!section?.NORM_CATEGORY?.find(
                  (category) => category == NormCategory.TRANSITIONAL_NORM
                )
            ),
        },
        {
          type: FieldType.RADIO,
          id: "divergentEntryIntoForceUndefinedSelection",
          label: "unbestimmt",
          values: norm.metadataSections?.DIVERGENT_ENTRY_INTO_FORCE?.map(
            (section) => !!section?.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED
          ),
        },
        {
          type: FieldType.DROPDOWN,
          id: "principleEntryIntoForceDateState",
          label: "Unbestimmtes grundsätzliches Inkrafttretedatum",
          values:
            norm.metadataSections?.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED?.map(
              (section) => section?.UNDEFINED_DATE?.[0]
            ).map(undefinedDateToDropdownEntry),
        },
        {
          type: FieldType.CHECKBOX,
          id: NormCategory.AMENDMENT_NORM,
          label: "Änderungsnorm",
          values:
            norm.metadataSections?.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED?.map(
              (section) =>
                !!section?.NORM_CATEGORY?.find(
                  (category) => category == NormCategory.AMENDMENT_NORM
                )
            ),
        },
        {
          type: FieldType.CHECKBOX,
          id: NormCategory.BASE_NORM,
          label: "Stammnorm",
          values:
            norm.metadataSections?.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED?.map(
              (section) =>
                !!section?.NORM_CATEGORY?.find(
                  (category) => category == NormCategory.BASE_NORM
                )
            ),
        },
        {
          type: FieldType.CHECKBOX,
          id: NormCategory.TRANSITIONAL_NORM,
          label: "Übergangsnorm",
          values:
            norm.metadataSections?.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED?.map(
              (section) =>
                !!section?.NORM_CATEGORY?.find(
                  (category) => category == NormCategory.TRANSITIONAL_NORM
                )
            ),
        },
      ],
    },
    {
      heading: "Abweichendes Außerkrafttretedatum",
      fields: [
        {
          type: FieldType.TEXT,
          id: "expirationDate",
          label: "Datum des Außerkrafttretens",
          value: norm.expirationDate,
        },
        {
          type: FieldType.DROPDOWN,
          id: "expirationDateState",
          label: "Unbestimmtes Datum des Außerkrafttretens",
          value: norm.expirationDateState,
        },
        {
          type: FieldType.CHECKBOX,
          id: "isExpirationDateTemp",
          label: "Befristet",
          value: norm.isExpirationDateTemp,
        },
        {
          type: FieldType.TEXT,
          id: "principleExpirationDate",
          label: "Grundsätzliches Außerkrafttretedatum",
          value: norm.principleExpirationDate,
        },
        {
          type: FieldType.DROPDOWN,
          id: "principleExpirationDateState",
          label: "Unbestimmtes grundsätzliches Außerkrafttretdatum",
          value: norm.principleExpirationDateState,
        },
        {
          type: FieldType.TEXT,
          id: "divergentExpirationDate",
          label: "Bestimmtes abweichendes Außerkrafttretedatum",
          value: norm.divergentExpirationDate,
        },
        {
          type: FieldType.DROPDOWN,
          id: "divergentExpirationDateState",
          label: "Unbestimmtes abweichendes Außerkrafttretdatum",
          value: norm.divergentExpirationDateState,
        },
        {
          type: FieldType.TEXT,
          id: "expirationNormCategory",
          label: "Art der Norm",
          value: norm.expirationNormCategory,
        },
      ],
    },
    {
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.TEXT,
          id: "announcementDate",
          label: "Verkündungsdatum",
          value: norm.announcementDate,
        },
      ],
    },
    {
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.TEXT,
          id: "publicationDate",
          label: "Veröffentlichungsdatum",
          value: norm.publicationDate,
        },
      ],
    },
    {
      heading: "Zitierdatum",
      id: "citationDates",
      isRepeatedSection: true,
      fields: [
        {
          type: FieldType.RADIO,
          id: "citationTypeDate",
          label: "Datum",
          values: norm.metadataSections?.CITATION_DATE?.map(
            (section) => !!section?.DATE
          ),
        },
        {
          type: FieldType.TEXT,
          id: "citationDate",
          label: "Jahresangabe",
          values: norm.metadataSections?.CITATION_DATE?.map(
            (section) => section?.DATE?.[0]
          ),
        },
        {
          type: FieldType.RADIO,
          id: "citationTypeYear",
          label: "Jahresangabe",
          values: norm.metadataSections?.CITATION_DATE?.map(
            (section) => !!section?.YEAR
          ),
        },
        {
          type: FieldType.TEXT,
          id: "citationYear",
          label: "Zitierdatum",
          values: norm.metadataSections?.CITATION_DATE?.map(
            (section) => section?.YEAR?.[0]
          ),
        },
      ],
    },
    {
      heading: "Amtliche Fundstelle",
      id: "officialReferences",
      isRepeatedSection: true,
      fields: [
        {
          type: FieldType.RADIO,
          id: "printAnnouncementSelection",
          label: "Papierverkündungsblatt",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => !!section?.PRINT_ANNOUNCEMENT
          ),
        },
        {
          type: FieldType.TEXT,
          id: "printAnnouncementGazette",
          label: "Verkündungsblatt",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.PRINT_ANNOUNCEMENT?.[0]
          ).map((section) => section?.ANNOUNCEMENT_GAZETTE?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "printAnnouncementYear",
          label: "Jahr",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.PRINT_ANNOUNCEMENT?.[0]
          ).map((section) => section?.YEAR?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "printAnnouncementNumber",
          label: "Nummer",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.PRINT_ANNOUNCEMENT?.[0]
          ).map((section) => section?.NUMBER?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "printAnnouncementPage",
          label: "Seitenzahl",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.PRINT_ANNOUNCEMENT?.[0]
          ).map((section) => section?.PAGE?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "printAnnouncementInfo",
          label: "Zusatzangaben",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.PRINT_ANNOUNCEMENT?.[0]
          ).map((section) => section?.ADDITIONAL_INFO?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "printAnnouncementExplanations",
          label: "Erläuterungen",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.PRINT_ANNOUNCEMENT?.[0]
          ).map((section) => section?.EXPLANATION?.[0]),
        },
        {
          type: FieldType.RADIO,
          id: "digitalAnnouncementSelection",
          label: "Elektronisches Verkündungsblatt",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => !!section?.DIGITAL_ANNOUNCEMENT
          ),
        },
        {
          type: FieldType.TEXT,
          id: "digitalAnnouncementMedium",
          label: "Verkündungsmedium",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.DIGITAL_ANNOUNCEMENT?.[0]
          ).map((section) => section?.ANNOUNCEMENT_MEDIUM?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "digitalAnnouncementDate",
          label: "Verkündungsdatum",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.DIGITAL_ANNOUNCEMENT?.[0]
          ).map((section) => section?.DATE?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "digitalAnnouncementEdition",
          label: "Ausgabenummer",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.DIGITAL_ANNOUNCEMENT?.[0]
          ).map((section) => section?.EDITION?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "digitalAnnouncementYear",
          label: "Jahr",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.DIGITAL_ANNOUNCEMENT?.[0]
          ).map((section) => section?.YEAR?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "digitalAnnouncementArea",
          label: "Bereich der Veröffentlichung",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.DIGITAL_ANNOUNCEMENT?.[0]
          ).map((section) => section?.AREA_OF_PUBLICATION?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "digitalAnnouncementAreaNumber",
          label: "Nummer der Veröffentlichung im jeweiligen Bereich",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.DIGITAL_ANNOUNCEMENT?.[0]
          ).map(
            (section) =>
              section?.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA?.[0]
          ),
        },
        {
          type: FieldType.TEXT,
          id: "digitalAnnouncementInfo",
          label: "Zusatzangaben",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.DIGITAL_ANNOUNCEMENT?.[0]
          ).map((section) => section?.ADDITIONAL_INFO?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "digitalAnnouncementExplanations",
          label: "Erläuterungen",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.DIGITAL_ANNOUNCEMENT?.[0]
          ).map((section) => section?.EXPLANATION?.[0]),
        },
        {
          type: FieldType.RADIO,
          id: "euAnnouncementSelection",
          label: "Amtsblatt der EU",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => !!section?.EU_ANNOUNCEMENT
          ),
        },
        {
          type: FieldType.TEXT,
          id: "euAnnouncementGazette",
          label: "Amtsblatt der EU",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.EU_ANNOUNCEMENT?.[0]
          ).map((section) => section?.EU_GOVERNMENT_GAZETTE?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "euAnnouncementYear",
          label: "Jahresangabe",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.EU_ANNOUNCEMENT?.[0]
          ).map((section) => section?.YEAR?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "euAnnouncementSeries",
          label: "Reihe",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.EU_ANNOUNCEMENT?.[0]
          ).map((section) => section?.SERIES?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "euAnnouncementNumber",
          label: "Nummer des Amtsblatts",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.EU_ANNOUNCEMENT?.[0]
          ).map((section) => section?.NUMBER?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "euAnnouncementPage",
          label: "Seitenzahl",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.EU_ANNOUNCEMENT?.[0]
          ).map((section) => section?.PAGE?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "euAnnouncementInfo",
          label: "Zusatzangaben",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.EU_ANNOUNCEMENT?.[0]
          ).map((section) => section?.ADDITIONAL_INFO?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "euAnnouncementExplanations",
          label: "Erläuterungen",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.EU_ANNOUNCEMENT?.[0]
          ).map((section) => section?.EXPLANATION?.[0]),
        },
        {
          type: FieldType.RADIO,
          id: "otherAnnouncementSelection",
          label: "Sonstige amtliche Fundstelle",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => !!section?.OTHER_OFFICIAL_ANNOUNCEMENT
          ),
        },
        {
          type: FieldType.TEXT,
          id: "otherOfficialAnnouncement",
          label: "Sonstige amtliche Fundstelle",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.OTHER_OFFICIAL_ANNOUNCEMENT?.[0]
          ).map((section) => section?.OTHER_OFFICIAL_REFERENCE?.[0]),
        },
      ],
    },
    {
      heading: "Nichtamtliche Fundstelle",
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.CHIPS,
          id: "unofficialReferences",
          label: "Nichtamtliche Fundstelle",
          value: norm.metadataSections?.NORM?.[0].UNOFFICIAL_REFERENCE,
        },
      ],
    },
    {
      heading: "Vollzitat",
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.TEXT,
          id: "completeCitation",
          label: "Vollzitat",
          value: norm.completeCitation,
        },
      ],
    },
    {
      heading: "Stand-Angabe",
      sections: [
        {
          heading: "Stand",
          fields: [
            {
              type: FieldType.TEXT,
              id: "statusNote",
              label: "Änderungshinweis",
              value: norm.statusNote,
            },
            {
              type: FieldType.TEXT,
              id: "statusDescription",
              label: "Bezeichnung der Änderungsvorschrift",
              value: norm.statusDescription,
            },
            {
              type: FieldType.TEXT,
              id: "statusDate",
              label: "Datum der Änderungsvorschrift",
              value: norm.statusDate,
            },
            {
              type: FieldType.TEXT,
              id: "statusReference",
              label: "Fundstellen der Änderungsvorschrift",
              value: norm.statusReference,
            },
          ],
        },
        {
          heading: "Aufhebung",
          fields: [
            {
              type: FieldType.TEXT,
              id: "repealNote",
              label: "Änderungshinweis",
              value: norm.repealNote,
            },
            {
              type: FieldType.TEXT,
              id: "repealArticle",
              label: "Artikel der Änderungsvorschrift",
              value: norm.repealArticle,
            },
            {
              type: FieldType.TEXT,
              id: "repealDate",
              label: "Datum der Änderungsvorschrift",
              value: norm.repealDate,
            },
            {
              type: FieldType.TEXT,
              id: "repealReferences",
              label: "Fundstellen der Änderungsvorschrift",
              value: norm.repealReferences,
            },
          ],
        },
        {
          heading: "Neufassung",
          fields: [
            {
              type: FieldType.TEXT,
              id: "reissueNote",
              label: "Neufassungshinweis",
              value: norm.reissueNote,
            },
            {
              type: FieldType.TEXT,
              id: "reissueArticle",
              label: "Bezeichnung der Bekanntmachung",
              value: norm.reissueArticle,
            },
            {
              type: FieldType.TEXT,
              id: "reissueDate",
              label: "Datum der Bekanntmachung",
              value: norm.reissueDate,
            },
            {
              type: FieldType.TEXT,
              id: "reissueReference",
              label: "Fundstelle der Bekanntmachung",
              value: norm.reissueReference,
            },
          ],
        },
        {
          heading: "Sonstiger Hinweis",
          fields: [
            {
              type: FieldType.TEXT,
              id: "otherStatusNote",
              label: "Sonstiger Hinweis",
              value: norm.otherStatusNote,
            },
          ],
        },
      ],
    },
    {
      heading: "Stand der dokumentarischen Bearbeitung",
      sections: [
        {
          heading: "Stand der dokumentarischen Bearbeitung",
          fields: [
            {
              type: FieldType.TEXT,
              id: "documentStatusWorkNote",
              label: "Bearbeitungshinweis",
              value: norm.documentStatusWorkNote,
            },
            {
              type: FieldType.TEXT,
              id: "documentStatusDescription",
              label: "Bezeichnung der Änderungsvorschrift",
              value: norm.documentStatusDescription,
            },
            {
              type: FieldType.TEXT,
              id: "documentStatusDate",
              label: "Datum der Änderungsvorschrift",
              value: norm.documentStatusDate,
            },
            {
              type: FieldType.TEXT,
              id: "documentStatusReference",
              label: "Fundstelle der Änderungsvorschrift",
              value: norm.documentStatusReference,
            },
            {
              type: FieldType.TEXT,
              id: "documentStatusEntryIntoForceDate",
              label: "Datum des Inkrafttretens der Änderung",
              value: norm.documentStatusEntryIntoForceDate,
            },
            {
              type: FieldType.TEXT,
              id: "documentStatusProof",
              label:
                "Angaben zum textlichen und/oder dokumentarischen Nachweis",
              value: norm.documentStatusProof,
            },
          ],
        },
        {
          heading: "Textnachweis",
          fields: [
            {
              type: FieldType.TEXT,
              id: "documentTextProof",
              label: "Textnachweis",
              value: norm.documentTextProof,
            },
          ],
        },
        {
          heading: "Sonstiger Hinweis",
          fields: [
            {
              type: FieldType.TEXT,
              id: "otherDocumentNote",
              label: "Sonstiger Hinweis",
              value: norm.otherDocumentNote,
            },
          ],
        },
      ],
    },
    {
      heading: "Aktivverweisung",
      fields: [
        {
          type: FieldType.TEXT,
          id: "categorizedReference",
          label: "Aktivverweisung",
          value: norm.categorizedReference,
        },
      ],
    },
    {
      heading: "Fußnoten",
      fields: [
        {
          type: FieldType.TEXT,
          id: "otherFootnote",
          label: "Sonstige Fußnote",
          value: norm.otherFootnote,
        },
        {
          type: FieldType.TEXT,
          id: "footnoteChange",
          label: "Änderungsfußnote",
          value: norm.footnoteChange,
        },
        {
          type: FieldType.TEXT,
          id: "footnoteComment",
          label: "Kommentierende Fußnote",
          value: norm.footnoteComment,
        },
        {
          type: FieldType.TEXT,
          id: "footnoteDecision",
          label: "BVerfG-Entscheidung",
          value: norm.footnoteDecision,
        },
        {
          type: FieldType.TEXT,
          id: "footnoteStateLaw",
          label: "Landesrecht",
          value: norm.footnoteStateLaw,
        },
        {
          type: FieldType.TEXT,
          id: "footnoteEuLaw",
          label: "EU/EG-Recht",
          value: norm.footnoteEuLaw,
        },
      ],
    },
    {
      heading: "Gültigkeitsregelung",
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.CHIPS,
          id: "validityRules",
          label: "Gültigkeitsregelung",
          value: norm.metadataSections?.NORM?.[0].VALIDITY_RULE,
        },
      ],
    },
    {
      heading: "Elektronischer Nachweis",
      fields: [
        {
          type: FieldType.TEXT,
          id: "digitalEvidenceLink",
          label: "Verlinkung",
          value: norm.digitalEvidenceLink,
        },
        {
          type: FieldType.TEXT,
          id: "digitalEvidenceRelatedData",
          label: "Zugehörige Dateien",
          value: norm.digitalEvidenceRelatedData,
        },
        {
          type: FieldType.TEXT,
          id: "digitalEvidenceExternalDataNote",
          label: "Hinweis auf fremde Verlinkung oder Daten",
          value: norm.digitalEvidenceExternalDataNote,
        },
        {
          type: FieldType.TEXT,
          id: "digitalEvidenceAppendix",
          label: "Zusatz zum Nachweis",
          value: norm.digitalEvidenceAppendix,
        },
      ],
    },
    {
      heading: "Aktenzeichen",
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.CHIPS,
          id: "referenceNumbers",
          label: "Aktenzeichen",
          value: "",
        },
      ],
    },
    {
      heading: "CELEX-Nummer",
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.TEXT,
          id: "celexNumber",
          label: "CELEX-Nummer",
          value: norm.celexNumber,
        },
      ],
    },
    {
      heading: "Altersangabe",
      id: "ageIndications",
      isRepeatedSection: true,
      fields: [
        {
          type: FieldType.TEXT,
          id: "ageIndicationStart",
          label: "Anfang",
          values: norm.metadataSections?.AGE_INDICATION?.map(
            (section) => section?.RANGE_START?.[0]
          ),
        },
        {
          type: FieldType.TEXT,
          id: "ageIndicationEnd",
          label: "Ende",
          values: norm.metadataSections?.AGE_INDICATION?.map(
            (section) => section?.RANGE_END?.[0]
          ),
        },
      ],
    },
    {
      heading: "Definition",
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.CHIPS,
          id: "definitions",
          label: "Definition",
          value: norm.metadataSections?.NORM?.[0]?.DEFINITION,
        },
      ],
    },
    {
      heading: "Angaben zur Volljährigkeit",
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.CHIPS,
          id: "ageOfMajorityIndications",
          label: "Angaben zur Volljährigkeit",
          value: norm.metadataSections?.NORM?.[0]?.AGE_OF_MAJORITY_INDICATION,
        },
      ],
    },
    {
      heading: "Text",
      isSingleFieldSection: true,
      fields: [
        {
          type: FieldType.TEXT,
          id: "text",
          label: "Text",
          value: norm.text,
        },
      ],
    },
  ]
}
