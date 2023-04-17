import { expect, test } from "@playwright/test"
import { Norm } from "../../../src/domain/Norm"
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

export type NormData = RecursiveOmit<Norm, "guid"> & {
  jurisZipFileName: string
}

export const testWithImportedNorm = test.extend<MyFixtures>({
  normData,
  guid: async ({ normData, request }, use) => {
    const fileName = normData["jurisZipFileName"]

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
      fields: [
        {
          type: FieldType.TEXT,
          name: "officialLongTitle",
          label: "Amtliche Langüberschrift",
          value: norm.officialLongTitle,
        },
        {
          type: FieldType.TEXT,
          name: "risAbbreviation",
          label: "Juris-Abkürzung",
          value: norm.risAbbreviation,
        },
        {
          type: FieldType.CHIPS,
          name: "risAbbreviationInternationalLaw",
          label: "Juris-Abkürzung für völkerrechtliche Vereinbarungen",
          value:
            norm.metadataSections?.NORM?.[0].RIS_ABBREVIATION_INTERNATIONAL_LAW,
        },
        {
          type: FieldType.TEXT,
          name: "documentNumber",
          label: "Dokumentnummer",
          value: norm.documentNumber,
        },
        {
          type: FieldType.CHIPS,
          name: "divergentDocumentNumber",
          label: "Abweichende Dokumentnummer",
          value: norm.metadataSections?.NORM?.[0].DIVERGENT_DOCUMENT_NUMBER,
        },
        {
          type: FieldType.TEXT,
          name: "documentCategory",
          label: "Dokumentart",
          value: norm.documentCategory,
        },
        {
          type: FieldType.CHIPS,
          name: "frameKeywords",
          label: "Schlagwörter im Rahmenelement",
          value: norm.metadataSections?.NORM?.[0].KEYWORD,
        },
      ],
    },
    {
      heading: "Dokumenttyp",
      fields: [
        {
          type: FieldType.TEXT,
          name: "documentTypeName",
          label: "Typbezeichnung",
          value: norm.documentTypeName,
        },
        {
          type: FieldType.TEXT,
          name: "documentNormCategory",
          label: "Art der Norm",
          value: norm.documentNormCategory,
        },
        {
          type: FieldType.TEXT,
          name: "documentTemplateName",
          label: "Bezeichnung gemäß Vorlage",
          value: norm.documentTemplateName,
        },
      ],
    },
    {
      heading: "Normgeber",
      fields: [
        {
          type: FieldType.TEXT,
          name: "providerEntity",
          label: "Staat, Land, Stadt, Landkreis oder juristische Person",
          value: norm.providerEntity,
        },
        {
          type: FieldType.TEXT,
          name: "providerDecidingBody",
          label: "Beschließendes Organ",
          value: norm.providerDecidingBody,
        },
        {
          type: FieldType.CHECKBOX,
          name: "providerIsResolutionMajority",
          label: "Beschlussfassung mit qualifizierter Mehrheit",
          value: norm.providerIsResolutionMajority,
        },
      ],
    },
    {
      heading: "Mitwirkende Organe",
      id: "participatingInstitutionsFields",
      isRepeatedSection: true,
      fields: [
        {
          type: FieldType.TEXT,
          name: "participationType",
          label: "Art der Mitwirkung",
          values: norm.metadataSections?.PARTICIPATION?.map(
            (section) => section?.PARTICIPATION_TYPE?.[0]
          ),
        },
        {
          type: FieldType.TEXT,
          name: "participationInstitution",
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
      id: "leadFields",
      fields: [
        {
          type: FieldType.TEXT,
          name: "leadJurisdiction",
          label: "Ressort",
          values: norm.metadataSections?.LEAD?.map(
            (section) => section?.LEAD_JURISDICTION?.[0]
          ),
        },
        {
          type: FieldType.TEXT,
          name: "leadUnit",
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
      id: "subjectAreaFields",
      fields: [
        {
          type: FieldType.TEXT,
          name: "subjectFna",
          label: "FNA-Nummer",
          values: norm.metadataSections?.SUBJECT_AREA?.map(
            (section) => section?.SUBJECT_FNA?.[0]
          ),
        },
        {
          type: FieldType.TEXT,
          name: "subjectPreviousFna",
          label: "Frühere FNA-Nummer",
          values: norm.metadataSections?.SUBJECT_AREA?.map(
            (section) => section?.SUBJECT_PREVIOUS_FNA?.[0]
          ),
        },
        {
          type: FieldType.TEXT,
          name: "subjectGesta",
          label: "GESTA-Nummer",
          values: norm.metadataSections?.SUBJECT_AREA?.map(
            (section) => section?.SUBJECT_GESTA?.[0]
          ),
        },
        {
          type: FieldType.TEXT,
          name: "subjectBgb3",
          label: "Bundesgesetzblatt Teil III",
          values: norm.metadataSections?.SUBJECT_AREA?.map(
            (section) => section?.SUBJECT_BGB_3?.[0]
          ),
        },
      ],
    },
    {
      heading: "Überschriften und Abkürzungen",
      fields: [
        {
          type: FieldType.TEXT,
          name: "officialShortTitle",
          label: "Amtliche Kurzüberschrift",
          value: norm.officialShortTitle,
        },
        {
          type: FieldType.TEXT,
          name: "officialAbbreviation",
          label: "Amtliche Buchstabenabkürzung",
          value: norm.officialAbbreviation,
        },
      ],
      sections: [
        {
          heading: "Nichtamtliche Überschriften und Abkürzungen",
          fields: [
            {
              type: FieldType.CHIPS,
              name: "unofficialLongTitle",
              label: "Nichtamtliche Langüberschrift",
              value: norm.metadataSections?.NORM?.[0].UNOFFICIAL_LONG_TITLE,
            },
            {
              type: FieldType.CHIPS,
              name: "unofficialShortTitle",
              label: "Nichtamtliche Kurzüberschrift",
              value: norm.metadataSections?.NORM?.[0].UNOFFICIAL_SHORT_TITLE,
            },
            {
              type: FieldType.CHIPS,
              name: "unofficialAbbreviation",
              label: "Nichtamtliche Buchstabenabkürzung",
              value: norm.metadataSections?.NORM?.[0].UNOFFICIAL_ABBREVIATION,
            },
          ],
        },
      ],
    },
    {
      heading: "Inkrafttreten",
      fields: [
        {
          type: FieldType.TEXT,
          name: "entryIntoForceDate",
          label: "Datum des Inkrafttretens",
          value: norm.entryIntoForceDate,
        },
        {
          type: FieldType.DROPDOWN,
          name: "entryIntoForceDateState",
          label: "Unbestimmtes Datum des Inkrafttretens",
          value: norm.entryIntoForceDateState,
        },
        {
          type: FieldType.TEXT,
          name: "principleEntryIntoForceDate",
          label: "Grundsätzliches Inkrafttretedatum",
          value: norm.principleEntryIntoForceDate,
        },
        {
          type: FieldType.DROPDOWN,
          name: "principleEntryIntoForceDateState",
          label: "Unbestimmtes grundsätzliches Inkrafttretedatum",
          value: norm.principleEntryIntoForceDateState,
        },
        {
          type: FieldType.TEXT,
          name: "divergentEntryIntoForceDate",
          label: "Bestimmtes abweichendes Inkrafttretedatum",
          value: norm.divergentEntryIntoForceDate,
        },
        {
          type: FieldType.DROPDOWN,
          name: "divergentEntryIntoForceDateState",
          label: "Unbestimmtes abweichendes Inkrafttretedatum",
          value: norm.divergentEntryIntoForceDateState,
        },
      ],
    },
    {
      heading: "Außerkrafttreten",
      fields: [
        {
          type: FieldType.TEXT,
          name: "expirationDate",
          label: "Datum des Außerkrafttretens",
          value: norm.expirationDate,
        },
        {
          type: FieldType.DROPDOWN,
          name: "expirationDateState",
          label: "Unbestimmtes Datum des Außerkrafttretens",
          value: norm.expirationDateState,
        },
        {
          type: FieldType.CHECKBOX,
          name: "isExpirationDateTemp",
          label: "Befristet",
          value: norm.isExpirationDateTemp,
        },
        {
          type: FieldType.TEXT,
          name: "principleExpirationDate",
          label: "Grundsätzliches Außerkrafttretedatum",
          value: norm.principleExpirationDate,
        },
        {
          type: FieldType.DROPDOWN,
          name: "principleExpirationDateState",
          label: "Unbestimmtes grundsätzliches Außerkrafttretdatum",
          value: norm.principleExpirationDateState,
        },
        {
          type: FieldType.TEXT,
          name: "divergentExpirationDate",
          label: "Bestimmtes abweichendes Außerkrafttretedatum",
          value: norm.divergentExpirationDate,
        },
        {
          type: FieldType.DROPDOWN,
          name: "divergentExpirationDateState",
          label: "Unbestimmtes abweichendes Außerkrafttretdatum",
          value: norm.divergentExpirationDateState,
        },
        {
          type: FieldType.TEXT,
          name: "expirationNormCategory",
          label: "Art der Norm",
          value: norm.expirationNormCategory,
        },
      ],
    },
    {
      heading: "Verkündungsdatum",
      fields: [
        {
          type: FieldType.TEXT,
          name: "announcementDate",
          label: "Verkündungsdatum",
          value: norm.announcementDate,
        },
        {
          type: FieldType.TEXT,
          name: "publicationDate",
          label: "Veröffentlichungsdatum",
          value: norm.publicationDate,
        },
      ],
    },
    {
      heading: "Zitierdatum",
      fields: [
        {
          type: FieldType.TEXT,
          name: "citationDate",
          label: "Zitierdatum",
          value: norm.citationDate,
        },
      ],
    },
    {
      heading: "Amtliche Fundstelle",
      sections: [
        {
          heading: "Papierverkündung",
          fields: [
            {
              type: FieldType.TEXT,
              name: "printAnnouncementGazette",
              label: "Verkündungsblatt",
              value: norm.printAnnouncementGazette,
            },
            {
              type: FieldType.TEXT,
              name: "printAnnouncementYear",
              label: "Jahr",
              value: norm.printAnnouncementYear,
            },
            {
              type: FieldType.TEXT,
              name: "printAnnouncementNumber",
              label: "Nummer",
              value: norm.printAnnouncementNumber,
            },
            {
              type: FieldType.TEXT,
              name: "printAnnouncementPage",
              label: "Seitenzahl",
              value: norm.printAnnouncementPage,
            },
            {
              type: FieldType.TEXT,
              name: "printAnnouncementInfo",
              label: "Zusatzangaben",
              value: norm.printAnnouncementInfo,
            },
            {
              type: FieldType.TEXT,
              name: "printAnnouncementExplanations",
              label: "Erläuterungen",
              value: norm.printAnnouncementExplanations,
            },
          ],
        },
        {
          heading: "Elektronisches Verkündungsblatt",
          fields: [
            {
              type: FieldType.TEXT,
              name: "digitalAnnouncementMedium",
              label: "Verkündungsmedium",
              value: norm.digitalAnnouncementMedium,
            },
            {
              type: FieldType.TEXT,
              name: "digitalAnnouncementDate",
              label: "Verkündungsdatum",
              value: norm.digitalAnnouncementDate,
            },
            {
              type: FieldType.TEXT,
              name: "digitalAnnouncementEdition",
              label: "Ausgabenummer",
              value: norm.digitalAnnouncementEdition,
            },
            {
              type: FieldType.TEXT,
              name: "digitalAnnouncementYear",
              label: "Jahr",
              value: norm.digitalAnnouncementYear,
            },
            {
              type: FieldType.TEXT,
              name: "digitalAnnouncementPage",
              label: "Seitenzahlen",
              value: norm.digitalAnnouncementPage,
            },
            {
              type: FieldType.TEXT,
              name: "digitalAnnouncementArea",
              label: "Bereich der Veröffentlichung",
              value: norm.digitalAnnouncementArea,
            },
            {
              type: FieldType.TEXT,
              name: "digitalAnnouncementAreaNumber",
              label: "Nummer der Veröffentlichung im jeweiligen Bereich",
              value: norm.digitalAnnouncementAreaNumber,
            },
            {
              type: FieldType.TEXT,
              name: "digitalAnnouncementInfo",
              label: "Zusatzangaben",
              value: norm.digitalAnnouncementInfo,
            },
            {
              type: FieldType.TEXT,
              name: "digitalAnnouncementExplanations",
              label: "Erläuterungen",
              value: norm.digitalAnnouncementExplanations,
            },
          ],
        },
        {
          heading: "Amtsblatt der EU",
          fields: [
            {
              type: FieldType.TEXT,
              name: "euAnnouncementGazette",
              label: "Amtsblatt der EU",
              value: norm.euAnnouncementGazette,
            },
            {
              type: FieldType.TEXT,
              name: "euAnnouncementYear",
              label: "Jahresangabe",
              value: norm.euAnnouncementYear,
            },
            {
              type: FieldType.TEXT,
              name: "euAnnouncementSeries",
              label: "Reihe",
              value: norm.euAnnouncementSeries,
            },
            {
              type: FieldType.TEXT,
              name: "euAnnouncementNumber",
              label: "Nummer des Amtsblatts",
              value: norm.euAnnouncementNumber,
            },
            {
              type: FieldType.TEXT,
              name: "euAnnouncementPage",
              label: "Seitenzahl",
              value: norm.euAnnouncementPage,
            },
            {
              type: FieldType.TEXT,
              name: "euAnnouncementInfo",
              label: "Zusatzangaben",
              value: norm.euAnnouncementInfo,
            },
            {
              type: FieldType.TEXT,
              name: "euAnnouncementExplanations",
              label: "Erläuterungen",
              value: norm.euAnnouncementExplanations,
            },
          ],
        },
        {
          heading: "Sonstige amtliche Fundstelle",
          fields: [
            {
              type: FieldType.TEXT,
              name: "otherOfficialAnnouncement",
              label: "Sonstige amtliche Fundstelle",
              value: norm.otherOfficialAnnouncement,
            },
          ],
        },
      ],
    },

    {
      heading: "Nichtamtliche Fundstelle",
      fields: [
        {
          type: FieldType.CHIPS,
          name: "unofficialReference",
          label: "Nichtamtliche Fundstelle",
          value: norm.metadataSections?.NORM?.[0].UNOFFICIAL_REFERENCE,
        },
      ],
    },
    {
      heading: "Vollzitat",
      fields: [
        {
          type: FieldType.TEXT,
          name: "completeCitation",
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
              name: "statusNote",
              label: "Änderungshinweis",
              value: norm.statusNote,
            },
            {
              type: FieldType.TEXT,
              name: "statusDescription",
              label: "Bezeichnung der Änderungsvorschrift",
              value: norm.statusDescription,
            },
            {
              type: FieldType.TEXT,
              name: "statusDate",
              label: "Datum der Änderungsvorschrift",
              value: norm.statusDate,
            },
            {
              type: FieldType.TEXT,
              name: "statusReference",
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
              name: "repealNote",
              label: "Änderungshinweis",
              value: norm.repealNote,
            },
            {
              type: FieldType.TEXT,
              name: "repealArticle",
              label: "Artikel der Änderungsvorschrift",
              value: norm.repealArticle,
            },
            {
              type: FieldType.TEXT,
              name: "repealDate",
              label: "Datum der Änderungsvorschrift",
              value: norm.repealDate,
            },
            {
              type: FieldType.TEXT,
              name: "repealReferences",
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
              name: "reissueNote",
              label: "Neufassungshinweis",
              value: norm.reissueNote,
            },
            {
              type: FieldType.TEXT,
              name: "reissueArticle",
              label: "Bezeichnung der Bekanntmachung",
              value: norm.reissueArticle,
            },
            {
              type: FieldType.TEXT,
              name: "reissueDate",
              label: "Datum der Bekanntmachung",
              value: norm.reissueDate,
            },
            {
              type: FieldType.TEXT,
              name: "reissueReference",
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
              name: "otherStatusNote",
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
              name: "documentStatusWorkNote",
              label: "Bearbeitungshinweis",
              value: norm.documentStatusWorkNote,
            },
            {
              type: FieldType.TEXT,
              name: "documentStatusDescription",
              label: "Bezeichnung der Änderungsvorschrift",
              value: norm.documentStatusDescription,
            },
            {
              type: FieldType.TEXT,
              name: "documentStatusDate",
              label: "Datum der Änderungsvorschrift",
              value: norm.documentStatusDate,
            },
            {
              type: FieldType.TEXT,
              name: "documentStatusReference",
              label: "Fundstelle der Änderungsvorschrift",
              value: norm.documentStatusReference,
            },
            {
              type: FieldType.TEXT,
              name: "documentStatusEntryIntoForceDate",
              label: "Datum des Inkrafttretens der Änderung",
              value: norm.documentStatusEntryIntoForceDate,
            },
            {
              type: FieldType.TEXT,
              name: "documentStatusProof",
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
              name: "documentTextProof",
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
              name: "otherDocumentNote",
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
          name: "categorizedReference",
          label: "Aktivverweisung",
          value: norm.categorizedReference,
        },
      ],
    },
    {
      heading: "Fußnote",
      fields: [
        {
          type: FieldType.TEXT,
          name: "otherFootnote",
          label: "Sonstige Fußnote",
          value: norm.otherFootnote,
        },
        {
          type: FieldType.TEXT,
          name: "footnoteChange",
          label: "Änderungsfußnote",
          value: norm.footnoteChange,
        },
        {
          type: FieldType.TEXT,
          name: "footnoteComment",
          label: "Kommentierende Fußnote",
          value: norm.footnoteComment,
        },
        {
          type: FieldType.TEXT,
          name: "footnoteDecision",
          label: "BVerfG-Entscheidung",
          value: norm.footnoteDecision,
        },
        {
          type: FieldType.TEXT,
          name: "footnoteStateLaw",
          label: "Landesrecht",
          value: norm.footnoteStateLaw,
        },
        {
          type: FieldType.TEXT,
          name: "footnoteEuLaw",
          label: "EU/EG-Recht",
          value: norm.footnoteEuLaw,
        },
      ],
    },
    {
      heading: "Gültigkeitsregelung",
      fields: [
        {
          type: FieldType.CHIPS,
          name: "validityRule",
          label: "Gültigkeitsregelung",
          value: "",
        },
      ],
    },
    {
      heading: "Elektronischer Nachweis",
      fields: [
        {
          type: FieldType.TEXT,
          name: "digitalEvidenceLink",
          label: "Verlinkung",
          value: norm.digitalEvidenceLink,
        },
        {
          type: FieldType.TEXT,
          name: "digitalEvidenceRelatedData",
          label: "Zugehörige Dateien",
          value: norm.digitalEvidenceRelatedData,
        },
        {
          type: FieldType.TEXT,
          name: "digitalEvidenceExternalDataNote",
          label: "Hinweis auf fremde Verlinkung oder Daten",
          value: norm.digitalEvidenceExternalDataNote,
        },
        {
          type: FieldType.TEXT,
          name: "digitalEvidenceAppendix",
          label: "Zusatz zum Nachweis",
          value: norm.digitalEvidenceAppendix,
        },
      ],
    },
    {
      heading: "Aktenzeichen",
      fields: [
        {
          type: FieldType.CHIPS,
          name: "referenceNumber",
          label: "Aktenzeichen",
          value: "",
        },
      ],
    },
    {
      heading: "CELEX-Nummer",
      fields: [
        {
          type: FieldType.TEXT,
          name: "celexNumber",
          label: "CELEX-Nummer",
          value: norm.celexNumber,
        },
      ],
    },
    {
      heading: "Altersangabe",
      fields: [
        {
          type: FieldType.TEXT,
          name: "ageIndicationStart",
          label: "Anfang",
          value: norm.ageIndicationStart,
        },
        {
          type: FieldType.TEXT,
          name: "ageIndicationEnd",
          label: "Ende",
          value: norm.ageIndicationEnd,
        },
      ],
    },
    {
      heading: "Definition",
      fields: [
        {
          type: FieldType.CHIPS,
          name: "definition",
          label: "Definition",
          value: norm.metadataSections?.NORM?.[0]?.DEFINITION,
        },
      ],
    },
    {
      heading: "Angaben zur Volljährigkeit",
      fields: [
        {
          type: FieldType.CHIPS,
          name: "ageOfMajorityIndication",
          label: "Angaben zur Volljährigkeit",
          value: norm.metadataSections?.NORM?.[0]?.AGE_OF_MAJORITY_INDICATION,
        },
      ],
    },
    {
      heading: "Text",
      fields: [
        {
          type: FieldType.TEXT,
          name: "text",
          label: "Text",
          value: norm.text,
        },
      ],
    },
  ]
}
