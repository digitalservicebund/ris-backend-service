import { expect, test } from "@playwright/test"
import { importNormViaApi, loadJurisTestFile } from "./e2e-utils"
import { normData } from "./testdata/norm_basic"
import { FieldType, MetadataInputSection } from "./utilities"
import { FOOTNOTE_LABELS } from "@/components/footnotes/types"
import {
  MetadataSectionName,
  Norm,
  NormCategory,
  OtherType,
  ProofIndication,
  ProofType,
  UndefinedDate,
} from "@/domain/Norm"

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

function proofIndicationToDropdownEntry(
  unit?: ProofIndication
): string | undefined {
  switch (unit) {
    case ProofIndication.NOT_YET_CONSIDERED:
      return "noch nicht berücksichtigt"
    case ProofIndication.CONSIDERED:
      return "ist berücksichtigt"
    default:
      return undefined
  }
}

function proofTypeToDropdownEntry(unit?: ProofType): string | undefined {
  switch (unit) {
    case ProofType.TEXT_PROOF_FROM:
      return "Textnachweis ab"
    case ProofType.TEXT_PROOF_VALIDITY_FROM:
      return "Textnachweis Geltung ab"
    default:
      return undefined
  }
}

function otherTypeToDropdownEntry(unit?: OtherType): string | undefined {
  switch (unit) {
    case OtherType.TEXT_IN_PROGRESS:
      return "Text in Bearbeitung"
    case OtherType.TEXT_PROOFED_BUT_NOT_DONE:
      return "Nachgewiesener Text dokumentarisch noch nicht abschließend bearbeitet"
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
          type: FieldType.TEXTAREA,
          id: "officialLongTitle",
          label: "Amtliche Langüberschrift",
          value: norm.metadataSections?.NORM?.[0]?.OFFICIAL_LONG_TITLE?.[0],
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
          value: norm.metadataSections?.NORM?.[0]?.RIS_ABBREVIATION?.[0],
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
          value: norm.metadataSections?.NORM?.[0]?.DOCUMENT_NUMBER?.[0],
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
          value: norm.metadataSections?.NORM?.[0]?.DOCUMENT_CATEGORY?.[0],
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
      isExpandableNotRepeatable: true,
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
          value: norm.metadataSections?.NORM?.[0]?.OFFICIAL_SHORT_TITLE?.[0],
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
          value: norm.metadataSections?.NORM?.[0]?.OFFICIAL_ABBREVIATION?.[0],
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
      heading: "Datum des Inkrafttretens",
      isExpandableNotRepeatable: true,
      id: "entryIntoForces",
      fields: [
        {
          type: FieldType.RADIO,
          id: "entryIntoForceSelection",
          label: "bestimmt",
          values: norm.metadataSections?.ENTRY_INTO_FORCE?.map(
            (section) => !!section?.DATE
          ),
        },
        {
          type: FieldType.TEXT,
          id: "entryIntoForceDate",
          label: "Bestimmtes Inkrafttretedatum",
          values: norm.metadataSections?.ENTRY_INTO_FORCE?.map(
            (section) => section?.DATE?.[0]
          ),
        },
        {
          type: FieldType.RADIO,
          id: "entryIntoForceUndefinedSelection",
          label: "unbestimmt",
          values: norm.metadataSections?.ENTRY_INTO_FORCE?.map(
            (section) => !!section?.UNDEFINED_DATE
          ),
        },
        {
          type: FieldType.DROPDOWN,
          id: "entryIntoForceUndefinedDateState",
          label: "Unbestimmtes Inkrafttretedatum",
          values: norm.metadataSections?.ENTRY_INTO_FORCE?.map((section) =>
            undefinedDateToDropdownEntry(section?.UNDEFINED_DATE?.[0])
          ),
        },
      ],
    },
    {
      heading: "Grundsätzliches Inkrafttretedatum",
      isExpandableNotRepeatable: true,
      id: "principleEntryIntoForces",
      fields: [
        {
          type: FieldType.RADIO,
          id: "principleEntryIntoForceSelection",
          label: "bestimmt",
          values: norm.metadataSections?.PRINCIPLE_ENTRY_INTO_FORCE?.map(
            (section) => !!section?.DATE
          ),
        },
        {
          type: FieldType.TEXT,
          id: "principleEntryIntoForceDate",
          label: "Bestimmtes grundsätzliches Inkrafttretedatum",
          values: norm.metadataSections?.PRINCIPLE_ENTRY_INTO_FORCE?.map(
            (section) => section?.DATE?.[0]
          ),
        },
        {
          type: FieldType.RADIO,
          id: "principleEntryIntoForceUndefinedSelection",
          label: "unbestimmt",
          values: norm.metadataSections?.PRINCIPLE_ENTRY_INTO_FORCE?.map(
            (section) => !!section?.UNDEFINED_DATE
          ),
        },
        {
          type: FieldType.DROPDOWN,
          id: "principleEntryIntoForceUndefinedDateState",
          label: "Unbestimmtes grundsätzliches Inkrafttretedatum",
          values: norm.metadataSections?.PRINCIPLE_ENTRY_INTO_FORCE?.map(
            (section) =>
              undefinedDateToDropdownEntry(section?.UNDEFINED_DATE?.[0])
          ),
        },
      ],
    },
    {
      heading: "Abweichendes Inkrafttretedatum",
      id: "divergentEntryIntoForces",
      isRepeatedSection: true,
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
          id: "divergentEntryIntoForceDefinedDateDateInput",
          label: "Bestimmtes abweichendes Inkrafttretedatum",
          values: norm.metadataSections?.DIVERGENT_ENTRY_INTO_FORCE?.map(
            (section) => section?.DIVERGENT_ENTRY_INTO_FORCE_DEFINED?.[0]
          ).map((section) => section?.DATE?.[0]),
        },
        {
          type: FieldType.CHECKBOX,
          id: [
            MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED,
            NormCategory.AMENDMENT_NORM,
          ].join("-"),
          label: "Änderungsnorm",
          values: norm.metadataSections?.DIVERGENT_ENTRY_INTO_FORCE?.map(
            (section) => section?.DIVERGENT_ENTRY_INTO_FORCE_DEFINED?.[0]
          ).map((section) =>
            section === undefined
              ? undefined
              : !!section?.NORM_CATEGORY?.find(
                  (category) => category == NormCategory.AMENDMENT_NORM
                )
          ),
        },
        {
          type: FieldType.CHECKBOX,
          id: [
            MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED,
            NormCategory.BASE_NORM,
          ].join("-"),
          label: "Stammnorm",
          values: norm.metadataSections?.DIVERGENT_ENTRY_INTO_FORCE?.map(
            (section) => section?.DIVERGENT_ENTRY_INTO_FORCE_DEFINED?.[0]
          ).map((section) =>
            section === undefined
              ? undefined
              : !!section?.NORM_CATEGORY?.find(
                  (category) => category == NormCategory.BASE_NORM
                )
          ),
        },
        {
          type: FieldType.CHECKBOX,
          id: [
            MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED,
            NormCategory.TRANSITIONAL_NORM,
          ].join("-"),
          label: "Übergangsnorm",
          values: norm.metadataSections?.DIVERGENT_ENTRY_INTO_FORCE?.map(
            (section) => section?.DIVERGENT_ENTRY_INTO_FORCE_DEFINED?.[0]
          ).map((section) =>
            section === undefined
              ? undefined
              : !!section?.NORM_CATEGORY?.find(
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
          id: "divergentEntryIntoForceUndefinedDateDropdown",
          label: "Unbestimmtes abweichendes Inkrafttretedatum",
          values: norm.metadataSections?.DIVERGENT_ENTRY_INTO_FORCE?.map(
            (section) => section?.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED?.[0]
          ).map((section) =>
            undefinedDateToDropdownEntry(section?.UNDEFINED_DATE?.[0])
          ),
        },
        {
          type: FieldType.CHECKBOX,
          id: [
            MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED,
            NormCategory.AMENDMENT_NORM,
          ].join("-"),
          label: "Änderungsnorm",
          values: norm.metadataSections?.DIVERGENT_ENTRY_INTO_FORCE?.map(
            (section) => section?.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED?.[0]
          ).map((section) =>
            section === undefined
              ? undefined
              : !!section?.NORM_CATEGORY?.find(
                  (category) => category == NormCategory.AMENDMENT_NORM
                )
          ),
        },
        {
          type: FieldType.CHECKBOX,
          id: [
            MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED,
            NormCategory.BASE_NORM,
          ].join("-"),
          label: "Stammnorm",
          values: norm.metadataSections?.DIVERGENT_ENTRY_INTO_FORCE?.map(
            (section) => section?.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED?.[0]
          ).map((section) =>
            section === undefined
              ? undefined
              : !!section?.NORM_CATEGORY?.find(
                  (category) => category == NormCategory.BASE_NORM
                )
          ),
        },
        {
          type: FieldType.CHECKBOX,
          id: [
            MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED,
            NormCategory.TRANSITIONAL_NORM,
          ].join("-"),
          label: "Übergangsnorm",
          values: norm.metadataSections?.DIVERGENT_ENTRY_INTO_FORCE?.map(
            (section) => section?.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED?.[0]
          ).map((section) =>
            section === undefined
              ? undefined
              : !!section?.NORM_CATEGORY?.find(
                  (category) => category == NormCategory.TRANSITIONAL_NORM
                )
          ),
        },
      ],
    },
    {
      heading: "Datum des Außerkrafttretens",
      isExpandableNotRepeatable: true,
      id: "expirations",
      fields: [
        {
          type: FieldType.RADIO,
          id: "expirationSelection",
          label: "bestimmt",
          values: norm.metadataSections?.EXPIRATION?.map(
            (section) => !!section?.DATE
          ),
        },
        {
          type: FieldType.TEXT,
          id: "expirationDate",
          label: "Bestimmtes Außerkrafttretedatum",
          values: norm.metadataSections?.EXPIRATION?.map(
            (section) => section?.DATE?.[0]
          ),
        },
        {
          type: FieldType.RADIO,
          id: "expirationUndefinedSelection",
          label: "unbestimmt",
          values: norm.metadataSections?.EXPIRATION?.map(
            (section) => !!section?.UNDEFINED_DATE
          ),
        },
        {
          type: FieldType.DROPDOWN,
          id: "expirationUndefinedDate",
          label: "Unbestimmtes Außerkrafttretedatum",
          values: norm.metadataSections?.EXPIRATION?.map((section) =>
            undefinedDateToDropdownEntry(section?.UNDEFINED_DATE?.[0])
          ),
        },
      ],
    },
    {
      heading: "Grundsätzliches Außerkrafttretedatum",
      isExpandableNotRepeatable: true,
      id: "principleExpirations",
      fields: [
        {
          type: FieldType.RADIO,
          id: "principleExpirationSelection",
          label: "bestimmt",
          values: norm.metadataSections?.PRINCIPLE_EXPIRATION?.map(
            (section) => !!section?.DATE
          ),
        },
        {
          type: FieldType.TEXT,
          id: "principleExpirationDate",
          label: "Bestimmtes grundsätzliches Außerkrafttretedatum",
          values: norm.metadataSections?.PRINCIPLE_EXPIRATION?.map(
            (section) => section?.DATE?.[0]
          ),
        },
        {
          type: FieldType.RADIO,
          id: "principleExpirationUndefinedSelection",
          label: "unbestimmt",
          values: norm.metadataSections?.PRINCIPLE_EXPIRATION?.map(
            (section) => !!section?.UNDEFINED_DATE
          ),
        },
        {
          type: FieldType.DROPDOWN,
          id: "principleExpirationUndefinedDate",
          label: "Unbestimmtes grundsätzliches Außerkrafttretedatum",
          values: norm.metadataSections?.PRINCIPLE_EXPIRATION?.map((section) =>
            undefinedDateToDropdownEntry(section?.UNDEFINED_DATE?.[0])
          ),
        },
      ],
    },
    {
      heading: "Abweichendes Außerkrafttretedatum",
      id: "divergentExpirations",
      isRepeatedSection: true,
      fields: [
        {
          type: FieldType.RADIO,
          id: "divergentExpirationDefinedSelection",
          label: "bestimmt",
          values: norm.metadataSections?.DIVERGENT_EXPIRATION?.map(
            (section) => !!section?.DIVERGENT_EXPIRATION_DEFINED
          ),
        },
        {
          type: FieldType.TEXT,
          id: "divergentExpirationDefinedDateDateInput",
          label: "Bestimmtes abweichendes Außerkrafttretedatum",
          values: norm.metadataSections?.DIVERGENT_EXPIRATION?.map(
            (section) => section?.DIVERGENT_EXPIRATION_DEFINED?.[0]
          ).map((section) => section?.DATE?.[0]),
        },
        {
          type: FieldType.CHECKBOX,
          id: [
            MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED,
            NormCategory.AMENDMENT_NORM,
          ].join("-"),
          label: "Änderungsnorm",
          values: norm.metadataSections?.DIVERGENT_EXPIRATION?.map(
            (section) => section?.DIVERGENT_EXPIRATION_DEFINED?.[0]
          ).map((section) =>
            section === undefined
              ? undefined
              : !!section?.NORM_CATEGORY?.find(
                  (category) => category == NormCategory.AMENDMENT_NORM
                )
          ),
        },
        {
          type: FieldType.CHECKBOX,
          id: [
            MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED,
            NormCategory.BASE_NORM,
          ].join("-"),
          label: "Stammnorm",
          values: norm.metadataSections?.DIVERGENT_EXPIRATION?.map(
            (section) => section?.DIVERGENT_EXPIRATION_DEFINED?.[0]
          ).map((section) =>
            section === undefined
              ? undefined
              : !!section?.NORM_CATEGORY?.find(
                  (category) => category == NormCategory.BASE_NORM
                )
          ),
        },
        {
          type: FieldType.CHECKBOX,
          id: [
            MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED,
            NormCategory.TRANSITIONAL_NORM,
          ].join("-"),
          label: "Übergangsnorm",
          values: norm.metadataSections?.DIVERGENT_EXPIRATION?.map(
            (section) => section?.DIVERGENT_EXPIRATION_DEFINED?.[0]
          ).map((section) =>
            section === undefined
              ? undefined
              : !!section?.NORM_CATEGORY?.find(
                  (category) => category == NormCategory.TRANSITIONAL_NORM
                )
          ),
        },
        {
          type: FieldType.RADIO,
          id: "divergentExpirationUndefinedSelection",
          label: "unbestimmt",
          values: norm.metadataSections?.DIVERGENT_EXPIRATION?.map(
            (section) => !!section?.DIVERGENT_EXPIRATION_UNDEFINED
          ),
        },
        {
          type: FieldType.DROPDOWN,
          id: "divergentExpirationUndefinedDateDropdown",
          label: "Unbestimmtes abweichendes Außerkrafttretedatum",
          values: norm.metadataSections?.DIVERGENT_EXPIRATION?.map(
            (section) => section?.DIVERGENT_EXPIRATION_UNDEFINED?.[0]
          )
            .map((section) => section?.UNDEFINED_DATE?.[0])
            .map(undefinedDateToDropdownEntry),
        },
        {
          type: FieldType.CHECKBOX,
          id: [
            MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED,
            NormCategory.AMENDMENT_NORM,
          ].join("-"),
          label: "Änderungsnorm",
          values: norm.metadataSections?.DIVERGENT_EXPIRATION?.map(
            (section) => section?.DIVERGENT_EXPIRATION_UNDEFINED?.[0]
          ).map((section) =>
            section === undefined
              ? undefined
              : !!section?.NORM_CATEGORY?.find(
                  (category) => category == NormCategory.AMENDMENT_NORM
                )
          ),
        },
        {
          type: FieldType.CHECKBOX,
          id: [
            MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED,
            NormCategory.BASE_NORM,
          ].join("-"),
          label: "Stammnorm",
          values: norm.metadataSections?.DIVERGENT_EXPIRATION?.map(
            (section) => section?.DIVERGENT_EXPIRATION_UNDEFINED?.[0]
          ).map((section) =>
            section === undefined
              ? undefined
              : !!section?.NORM_CATEGORY?.find(
                  (category) => category == NormCategory.BASE_NORM
                )
          ),
        },
        {
          type: FieldType.CHECKBOX,
          id: [
            MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED,
            NormCategory.TRANSITIONAL_NORM,
          ].join("-"),
          label: "Übergangsnorm",
          values: norm.metadataSections?.DIVERGENT_EXPIRATION?.map(
            (section) => section?.DIVERGENT_EXPIRATION_UNDEFINED?.[0]
          ).map((section) =>
            section === undefined
              ? undefined
              : !!section?.NORM_CATEGORY?.find(
                  (category) => category == NormCategory.TRANSITIONAL_NORM
                )
          ),
        },
      ],
    },
    {
      heading: "Verkündungsdatum",
      id: "announcementDate",
      isExpandableNotRepeatable: true,
      fields: [
        {
          type: FieldType.RADIO,
          id: "announcementDate",
          label: "Datum",
          values: [
            norm.metadataSections?.ANNOUNCEMENT_DATE?.some(
              (section) => !!section?.DATE
            ),
          ],
        },
        {
          type: FieldType.TEXT,
          id: "announcementDateInput",
          label: "Datum",
          values: norm.metadataSections?.ANNOUNCEMENT_DATE?.map(
            (section) => section?.DATE?.[0]
          ),
        },
        {
          type: FieldType.TEXT,
          id: "announcementDateTime",
          label: "Uhrzeit",
          values: norm.metadataSections?.ANNOUNCEMENT_DATE?.map(
            (section) => section?.TIME?.[0]
          ),
        },
        {
          type: FieldType.RADIO,
          id: "announcementYear",
          label: "Jahresangabe",
          values: norm.metadataSections?.ANNOUNCEMENT_DATE?.map(
            (section) => !!section?.YEAR
          ),
        },
        {
          type: FieldType.TEXT,
          id: "announcementDateYearInput",
          label: "Jahresangabe",
          values: norm.metadataSections?.ANNOUNCEMENT_DATE?.map(
            (section) => section?.YEAR?.[0]
          ),
        },
      ],
    },
    {
      heading: "Veröffentlichungsdatum",
      isExpandableNotRepeatable: true,
      isNotImported: true,
      id: "publicationDates",
      fields: [
        {
          type: FieldType.RADIO,
          id: "publicationTypeDate",
          label: "Datum",
          values: norm.metadataSections?.PUBLICATION_DATE?.map(
            (section) => !!section?.DATE
          ),
        },
        {
          type: FieldType.TEXT,
          id: "publicationDate",
          label: "Jahresangabe",
          values: norm.metadataSections?.PUBLICATION_DATE?.map(
            (section) => section?.DATE?.[0]
          ),
        },
        {
          type: FieldType.RADIO,
          id: "publicationTypeYear",
          label: "Jahresangabe",
          values: norm.metadataSections?.PUBLICATION_DATE?.map(
            (section) => !!section?.YEAR
          ),
        },
        {
          type: FieldType.TEXT,
          id: "publicationYear",
          label: "Veröffentlichungsdatum",
          values: norm.metadataSections?.PUBLICATION_DATE?.map(
            (section) => section?.YEAR?.[0]
          ),
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
          type: FieldType.CHIPS,
          id: "printAnnouncementInfo",
          label: "Zusatzangaben",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.PRINT_ANNOUNCEMENT?.[0]
          ).map((section) => section?.ADDITIONAL_INFO),
        },
        {
          type: FieldType.CHIPS,
          id: "printAnnouncementExplanations",
          label: "Erläuterungen",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.PRINT_ANNOUNCEMENT?.[0]
          ).map((section) => section?.EXPLANATION),
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
          type: FieldType.CHIPS,
          id: "digitalAnnouncementInfo",
          label: "Zusatzangaben",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.DIGITAL_ANNOUNCEMENT?.[0]
          ).map((section) => section?.ADDITIONAL_INFO),
        },
        {
          type: FieldType.CHIPS,
          id: "digitalAnnouncementExplanations",
          label: "Erläuterungen",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.DIGITAL_ANNOUNCEMENT?.[0]
          ).map((section) => section?.EXPLANATION),
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
          type: FieldType.CHIPS,
          id: "euAnnouncementInfo",
          label: "Zusatzangaben",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.EU_ANNOUNCEMENT?.[0]
          ).map((section) => section?.ADDITIONAL_INFO),
        },
        {
          type: FieldType.CHIPS,
          id: "euAnnouncementExplanations",
          label: "Erläuterungen",
          values: norm.metadataSections?.OFFICIAL_REFERENCE?.map(
            (section) => section?.EU_ANNOUNCEMENT?.[0]
          ).map((section) => section?.EXPLANATION),
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
          value: norm.metadataSections?.NORM?.[0]?.COMPLETE_CITATION?.[0],
        },
      ],
    },
    {
      heading: "Stand-Angabe",
      id: "statusIndication",
      isRepeatedSection: true,
      numberEditedSections: 4,
      fields: [
        {
          type: FieldType.RADIO,
          id: "statusSelection",
          label: "Stand",
          values: norm.metadataSections?.STATUS_INDICATION?.map(
            (section) => !!section?.STATUS
          ),
        },
        {
          type: FieldType.TEXT,
          id: "statusNote",
          label: "Änderungshinweis",
          values: norm.metadataSections?.STATUS_INDICATION?.map(
            (section) => section?.STATUS?.[0]
          ).map((section) => section?.NOTE?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "statusDescription",
          label: "Bezeichnung der Änderungsvorschrift",
          values: norm.metadataSections?.STATUS_INDICATION?.map(
            (section) => section?.STATUS?.[0]
          ).map((section) => section?.DESCRIPTION?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "statusDate",
          label: "Datum der Änderungsvorschrift",
          values: norm.metadataSections?.STATUS_INDICATION?.map(
            (section) => section?.STATUS?.[0]
          ).map((section) => section?.DATE?.[0]),
        },
        {
          type: FieldType.CHIPS,
          id: "statusReference",
          label: "Fundstellen der Änderungsvorschrift",
          values: norm.metadataSections?.STATUS_INDICATION?.map(
            (section) => section?.STATUS?.[0]
          ).map((section) => section?.REFERENCE),
        },
        {
          type: FieldType.RADIO,
          id: "reissueSelection",
          label: "Neufassung",
          values: norm.metadataSections?.STATUS_INDICATION?.map(
            (section) => !!section?.REISSUE
          ),
        },
        {
          type: FieldType.TEXT,
          id: "reissueNote",
          label: "Neufassungshinweis",
          values: norm.metadataSections?.STATUS_INDICATION?.map(
            (section) => section?.REISSUE?.[0]
          ).map((section) => section?.NOTE?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "reissueArticle",
          label: "Bezeichnung der Bekanntmachung",
          values: norm.metadataSections?.STATUS_INDICATION?.map(
            (section) => section?.REISSUE?.[0]
          ).map((section) => section?.ARTICLE?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "reissueDate",
          label: "Datum der Bekanntmachung",
          values: norm.metadataSections?.STATUS_INDICATION?.map(
            (section) => section?.REISSUE?.[0]
          ).map((section) => section?.DATE?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "reissueReference",
          label: "Fundstelle der Bekanntmachung",
          values: norm.metadataSections?.STATUS_INDICATION?.map(
            (section) => section?.REISSUE?.[0]
          ).map((section) => section?.REFERENCE?.[0]),
        },
        {
          type: FieldType.RADIO,
          id: "repealSelection",
          label: "Aufhebung",
          values: norm.metadataSections?.STATUS_INDICATION?.map(
            (section) => !!section?.REPEAL
          ),
        },
        {
          type: FieldType.TEXTAREA,
          id: "repealText",
          label: "Aufhebung",
          values: norm.metadataSections?.STATUS_INDICATION?.map(
            (section) => section?.REPEAL?.[0]
          ).map((section) => section?.TEXT?.[0]),
        },
        {
          type: FieldType.RADIO,
          id: "otherStatusSelection",
          label: "Sonstiger Hinweis",
          values: norm.metadataSections?.STATUS_INDICATION?.map(
            (section) => !!section?.OTHER_STATUS
          ),
        },
        {
          type: FieldType.TEXTAREA,
          id: "otherStatusNote",
          label: "Sonstiger Hinweis",
          values: norm.metadataSections?.STATUS_INDICATION?.map(
            (section) => section?.OTHER_STATUS?.[0]
          ).map((section) => section?.NOTE?.[0]),
        },
      ],
    },
    {
      heading: "Stand der dokumentarischen Bearbeitung",
      id: "documentStatus",
      isRepeatedSection: true,
      numberEditedSections: 3,
      fields: [
        {
          type: FieldType.RADIO,
          id: "documentStatusSelection",
          label: "Stand der dokumentarischen Bearbeitung",
          values: norm.metadataSections?.DOCUMENT_STATUS_SECTION?.map(
            (section) => !!section?.DOCUMENT_STATUS
          ),
        },
        {
          type: FieldType.CHIPS,
          id: "workNoteChips",
          label: "Bearbeitungshinweis",
          values: norm.metadataSections?.DOCUMENT_STATUS_SECTION?.map(
            (section) => section?.DOCUMENT_STATUS?.[0]
          ).map((section) => section?.WORK_NOTE),
        },
        {
          type: FieldType.TEXT,
          id: "descriptionText",
          label: "Bezeichnung der Änderungsvorschrift",
          values: norm.metadataSections?.DOCUMENT_STATUS_SECTION?.map(
            (section) => section?.DOCUMENT_STATUS?.[0]
          ).map((section) => section?.TEXT?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "documentStatusDate",
          label: "Datum",
          values: norm.metadataSections?.DOCUMENT_STATUS_SECTION?.map(
            (section) => section?.DOCUMENT_STATUS?.[0]
          ).map((section) => section?.DATE?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "documentStatusYear",
          label: "Jahr",
          values: norm.metadataSections?.DOCUMENT_STATUS_SECTION?.map(
            (section) => section?.DOCUMENT_STATUS?.[0]
          ).map((section) => section?.YEAR?.[0]),
        },
        {
          type: FieldType.TEXT,
          id: "referenceText",
          label: "Fundstelle der Änderungsvorschrift",
          values: norm.metadataSections?.DOCUMENT_STATUS_SECTION?.map(
            (section) => section?.DOCUMENT_STATUS?.[0]
          ).map((section) => section?.REFERENCE?.[0]),
        },
        {
          type: FieldType.CHIPS,
          id: "entryIntoForceDateNoteChips",
          label: "Datum des Inkrafttretens der Änderung",
          values: norm.metadataSections?.DOCUMENT_STATUS_SECTION?.map(
            (section) => section?.DOCUMENT_STATUS?.[0]
          ).map((section) => section?.ENTRY_INTO_FORCE_DATE_NOTE),
        },
        {
          type: FieldType.DROPDOWN,
          id: "proofIndicationDropdown",
          label: "Angaben zum textlichen und/oder dokumentarischen Nachweis",
          values: norm.metadataSections?.DOCUMENT_STATUS_SECTION?.map(
            (section) => section?.DOCUMENT_STATUS?.[0]
          )
            .map((section) => section?.PROOF_INDICATION?.[0])
            .map(proofIndicationToDropdownEntry),
        },
        {
          type: FieldType.RADIO,
          id: "documentTextProofSelection",
          label: "Textnachweis",
          values: norm.metadataSections?.DOCUMENT_STATUS_SECTION?.map(
            (section) => !!section?.DOCUMENT_TEXT_PROOF
          ),
        },
        {
          type: FieldType.DROPDOWN,
          id: "proofTypeDropdown",
          label: "Textnachweis",
          values: norm.metadataSections?.DOCUMENT_STATUS_SECTION?.map(
            (section) => section?.DOCUMENT_TEXT_PROOF?.[0]
          )
            .map((section) => section?.PROOF_TYPE?.[0])
            .map(proofTypeToDropdownEntry),
        },
        {
          type: FieldType.TEXT,
          id: "textInput",
          label: "Zusatz",
          values: norm.metadataSections?.DOCUMENT_STATUS_SECTION?.map(
            (section) => section?.DOCUMENT_TEXT_PROOF?.[0]
          ).map((section) => section?.TEXT?.[0]),
        },
        {
          type: FieldType.RADIO,
          id: "documentOtherSelection",
          label: "Sonstiger Hinweis",
          values: norm.metadataSections?.DOCUMENT_STATUS_SECTION?.map(
            (section) => !!section?.DOCUMENT_OTHER
          ),
        },
        {
          type: FieldType.DROPDOWN,
          id: "otherTypeDropdown",
          label: "Sonstiger Hinweis",
          values: norm.metadataSections?.DOCUMENT_STATUS_SECTION?.map(
            (section) => section?.DOCUMENT_OTHER?.[0]
          )
            .map((section) => section?.OTHER_TYPE?.[0])
            .map(otherTypeToDropdownEntry),
        },
      ],
    },
    {
      heading: "Aktivverweisung",
      isRepeatedSection: true,
      id: "categorizedReferences",
      fields: [
        {
          type: FieldType.TEXT,
          id: "categorizedReferenceText",
          label: "Aktivverweisung",
          values: norm.metadataSections?.CATEGORIZED_REFERENCE?.map(
            (section) => section?.TEXT?.[0]
          ),
        },
      ],
    },
    {
      heading: "Fußnoten",
      isRepeatedSection: true,
      numberEditedSections: 1,
      id: "footnotes",
      fields: [
        {
          type: FieldType.EDITOR,
          id: "footnotes",
          label: "Fußnoten",
          values: norm.metadataSections?.FOOTNOTES?.map(
            (section) =>
              section.FOOTNOTE?.map((note) => ({
                label: FOOTNOTE_LABELS[Object.keys(note)[0]],
                content: Object.values(note)[0][0] as string,
              }))?.flat() ?? []
          ),
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
      isExpandableNotRepeatable: true,
      isNotImported: true,
      id: "digitalEvidence",
      fields: [
        {
          type: FieldType.TEXT,
          id: "digitalEvidenceLink",
          label: "Verlinkung",
          values: norm.metadataSections?.DIGITAL_EVIDENCE?.map(
            (section) => section?.LINK?.[0]
          ),
        },
        {
          type: FieldType.TEXT,
          id: "digitalEvidenceRelatedData",
          label: "Zugehörige Daten",
          values: norm.metadataSections?.DIGITAL_EVIDENCE?.map(
            (section) => section?.RELATED_DATA?.[0]
          ),
        },
        {
          type: FieldType.TEXT,
          id: "digitalEvidenceExternalDataNote",
          label: "Hinweis auf fremde Verlinkung oder Daten",
          values: norm.metadataSections?.DIGITAL_EVIDENCE?.map(
            (section) => section?.EXTERNAL_DATA_NOTE?.[0]
          ),
        },
        {
          type: FieldType.TEXT,
          id: "digitalEvidenceAppendix",
          label: "Zusatz zum Nachweis",
          values: norm.metadataSections?.DIGITAL_EVIDENCE?.map(
            (section) => section?.APPENDIX?.[0]
          ),
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
          value: norm.metadataSections?.NORM?.[0]?.REFERENCE_NUMBER,
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
          value: norm.metadataSections?.NORM?.[0]?.CELEX_NUMBER?.[0],
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
          value: norm.metadataSections?.NORM?.[0]?.TEXT?.[0],
        },
      ],
    },
  ]
}
