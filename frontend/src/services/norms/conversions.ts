import dayjs from "dayjs"
import timezone from "dayjs/plugin/timezone"
import utc from "dayjs/plugin/utc"
import {
  FlatMetadataRequestSchema,
  MetadataSectionSchema,
  MetadatumSchema,
  NormResponseSchema,
} from "./schemas"
import {
  compareOrder,
  filterEntries,
  groupBy,
  mapValues,
  mergeValues,
} from "./utilities"

import {
  FlatMetadata,
  Metadata,
  MetadataSectionName,
  MetadataSections,
  MetadataValueType,
  MetadatumType,
  Norm,
  NormCategory,
  OtherType,
  ProofIndication,
  ProofType,
  UndefinedDate,
} from "@/domain/Norm"

function identity<T>(data: T): T {
  return data
}

function encodeString(data?: string | null): string | null {
  return data && data.length > 0 ? data : null
}

// Makes the assumption that we currently get a date string in the following
// format: `2022-11-14T23:00:00.000Z`. To comply with the expected date format
// of the API, we only take the first 10 characters.
//
function encodeDate(data?: string): string {
  dayjs.extend(utc)
  dayjs.extend(timezone)

  return data && data.length > 0
    ? dayjs.utc(data).tz(dayjs.tz.guess()).format("YYYY-MM-DD")
    : ""
}

type MetadataValueDecoders = {
  [Property in keyof MetadataValueType]: (
    data: MetadatumSchema["value"]
  ) => MetadataValueType[Property]
}

type MetadataValueEncoders = {
  [Property in keyof MetadataValueType]: (
    data: MetadataValueType[Property]
  ) => MetadatumSchema["value"]
}

function decodeBoolean(data: string): boolean {
  return JSON.parse(data)
}

function encodeBoolean(data: boolean): string {
  return String(data)
}

function decodeNormCategory(data: string): NormCategory {
  const decodedCategory = Object.values(NormCategory).find(
    (category) => category == data
  )
  if (decodedCategory) return decodedCategory
  else throw new Error(`Unknown Norm Category: "${data}"`)
}

function decodeUndefinedDate(data: string): UndefinedDate {
  const indexOfKeyPassed = Object.keys(UndefinedDate).indexOf(data)

  const unit = Object.values(UndefinedDate)[indexOfKeyPassed]

  if (unit) {
    return unit
  } else throw new Error(`Could not decode UndefinedDate: '${data}'`)
}

function decodeProofIndication(data: string): ProofIndication {
  const indexOfKeyPassed = Object.keys(ProofIndication).indexOf(data)

  const unit = Object.values(ProofIndication)[indexOfKeyPassed]

  if (unit) {
    return unit
  } else throw new Error(`Could not decode ProofIndication: '${data}'`)
}

function decodeProofType(data: string): ProofType {
  const indexOfKeyPassed = Object.keys(ProofType).indexOf(data)

  const unit = Object.values(ProofType)[indexOfKeyPassed]

  if (unit) {
    return unit
  } else throw new Error(`Could not decode ProofType: '${data}'`)
}

function decodeOtherType(data: string): OtherType {
  const indexOfKeyPassed = Object.keys(OtherType).indexOf(data)

  const unit = Object.values(OtherType)[indexOfKeyPassed]

  if (unit) {
    return unit
  } else throw new Error(`Could not decode OtherType: '${data}'`)
}

function encodeUndefinedDate(data: UndefinedDate): string {
  return data
}

function encodeProofIndication(data: ProofIndication): string {
  return data
}

function encodeProofType(data: ProofType): string {
  return data
}

function encodeOtherType(data: OtherType): string {
  return data
}

const DECODERS: MetadataValueDecoders = {
  [MetadatumType.KEYWORD]: identity,
  [MetadatumType.UNOFFICIAL_LONG_TITLE]: identity,
  [MetadatumType.UNOFFICIAL_SHORT_TITLE]: identity,
  [MetadatumType.UNOFFICIAL_ABBREVIATION]: identity,
  [MetadatumType.UNOFFICIAL_REFERENCE]: identity,
  [MetadatumType.DIVERGENT_DOCUMENT_NUMBER]: identity,
  [MetadatumType.REFERENCE_NUMBER]: identity,
  [MetadatumType.DEFINITION]: identity,
  [MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW]: identity,
  [MetadatumType.AGE_OF_MAJORITY_INDICATION]: identity,
  [MetadatumType.VALIDITY_RULE]: identity,
  [MetadatumType.LEAD_JURISDICTION]: identity,
  [MetadatumType.LEAD_UNIT]: identity,
  [MetadatumType.PARTICIPATION_TYPE]: identity,
  [MetadatumType.PARTICIPATION_INSTITUTION]: identity,
  [MetadatumType.SUBJECT_FNA]: identity,
  [MetadatumType.SUBJECT_PREVIOUS_FNA]: identity,
  [MetadatumType.SUBJECT_GESTA]: identity,
  [MetadatumType.SUBJECT_BGB_3]: identity,
  [MetadatumType.DATE]: identity,
  [MetadatumType.YEAR]: identity,
  [MetadatumType.RANGE_START]: identity,
  [MetadatumType.RANGE_END]: identity,
  [MetadatumType.ANNOUNCEMENT_MEDIUM]: identity,
  [MetadatumType.ANNOUNCEMENT_GAZETTE]: identity,
  [MetadatumType.ADDITIONAL_INFO]: identity,
  [MetadatumType.EXPLANATION]: identity,
  [MetadatumType.AREA_OF_PUBLICATION]: identity,
  [MetadatumType.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA]: identity,
  [MetadatumType.EU_GOVERNMENT_GAZETTE]: identity,
  [MetadatumType.SERIES]: identity,
  [MetadatumType.OTHER_OFFICIAL_REFERENCE]: identity,
  [MetadatumType.NUMBER]: identity,
  [MetadatumType.PAGE]: identity,
  [MetadatumType.EDITION]: identity,
  [MetadatumType.ENTITY]: identity,
  [MetadatumType.DECIDING_BODY]: identity,
  [MetadatumType.RESOLUTION_MAJORITY]: decodeBoolean,
  [MetadatumType.TYPE_NAME]: identity,
  [MetadatumType.NORM_CATEGORY]: decodeNormCategory,
  [MetadatumType.TEMPLATE_NAME]: identity,
  [MetadatumType.UNDEFINED_DATE]: decodeUndefinedDate,
  [MetadatumType.TEXT]: identity,
  [MetadatumType.LINK]: identity,
  [MetadatumType.RELATED_DATA]: identity,
  [MetadatumType.EXTERNAL_DATA_NOTE]: identity,
  [MetadatumType.APPENDIX]: identity,
  [MetadatumType.FOOTNOTE_REFERENCE]: identity,
  [MetadatumType.FOOTNOTE_CHANGE]: identity,
  [MetadatumType.FOOTNOTE_COMMENT]: identity,
  [MetadatumType.FOOTNOTE_DECISION]: identity,
  [MetadatumType.FOOTNOTE_STATE_LAW]: identity,
  [MetadatumType.FOOTNOTE_EU_LAW]: identity,
  [MetadatumType.FOOTNOTE_OTHER]: identity,
  [MetadatumType.WORK_NOTE]: identity,
  [MetadatumType.DESCRIPTION]: identity,
  [MetadatumType.REFERENCE]: identity,
  [MetadatumType.ENTRY_INTO_FORCE_DATE_NOTE]: identity,
  [MetadatumType.PROOF_INDICATION]: decodeProofIndication,
  [MetadatumType.PROOF_TYPE]: decodeProofType,
  [MetadatumType.OTHER_TYPE]: decodeOtherType,
  [MetadatumType.NOTE]: identity,
  [MetadatumType.ARTICLE]: identity,
  [MetadatumType.OFFICIAL_LONG_TITLE]: identity,
  [MetadatumType.RIS_ABBREVIATION]: identity,
  [MetadatumType.DOCUMENT_NUMBER]: identity,
  [MetadatumType.DOCUMENT_CATEGORY]: identity,
  [MetadatumType.OFFICIAL_SHORT_TITLE]: identity,
  [MetadatumType.OFFICIAL_ABBREVIATION]: identity,
  [MetadatumType.COMPLETE_CITATION]: identity,
  [MetadatumType.CELEX_NUMBER]: identity,
  [MetadatumType.TIME]: identity,
}

const ENCODERS: MetadataValueEncoders = {
  [MetadatumType.KEYWORD]: identity,
  [MetadatumType.UNOFFICIAL_LONG_TITLE]: identity,
  [MetadatumType.UNOFFICIAL_SHORT_TITLE]: identity,
  [MetadatumType.UNOFFICIAL_ABBREVIATION]: identity,
  [MetadatumType.UNOFFICIAL_REFERENCE]: identity,
  [MetadatumType.DIVERGENT_DOCUMENT_NUMBER]: identity,
  [MetadatumType.REFERENCE_NUMBER]: identity,
  [MetadatumType.DEFINITION]: identity,
  [MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW]: identity,
  [MetadatumType.AGE_OF_MAJORITY_INDICATION]: identity,
  [MetadatumType.VALIDITY_RULE]: identity,
  [MetadatumType.LEAD_JURISDICTION]: identity,
  [MetadatumType.LEAD_UNIT]: identity,
  [MetadatumType.PARTICIPATION_TYPE]: identity,
  [MetadatumType.PARTICIPATION_INSTITUTION]: identity,
  [MetadatumType.SUBJECT_FNA]: identity,
  [MetadatumType.SUBJECT_PREVIOUS_FNA]: identity,
  [MetadatumType.SUBJECT_GESTA]: identity,
  [MetadatumType.SUBJECT_BGB_3]: identity,
  [MetadatumType.DATE]: encodeDate,
  [MetadatumType.YEAR]: identity,
  [MetadatumType.RANGE_START]: identity,
  [MetadatumType.RANGE_END]: identity,
  [MetadatumType.ANNOUNCEMENT_MEDIUM]: identity,
  [MetadatumType.ANNOUNCEMENT_GAZETTE]: identity,
  [MetadatumType.ADDITIONAL_INFO]: identity,
  [MetadatumType.EXPLANATION]: identity,
  [MetadatumType.AREA_OF_PUBLICATION]: identity,
  [MetadatumType.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA]: identity,
  [MetadatumType.EU_GOVERNMENT_GAZETTE]: identity,
  [MetadatumType.SERIES]: identity,
  [MetadatumType.OTHER_OFFICIAL_REFERENCE]: identity,
  [MetadatumType.NUMBER]: identity,
  [MetadatumType.PAGE]: identity,
  [MetadatumType.EDITION]: identity,
  [MetadatumType.ENTITY]: identity,
  [MetadatumType.DECIDING_BODY]: identity,
  [MetadatumType.RESOLUTION_MAJORITY]: encodeBoolean,
  [MetadatumType.TYPE_NAME]: identity,
  [MetadatumType.NORM_CATEGORY]: identity,
  [MetadatumType.TEMPLATE_NAME]: identity,
  [MetadatumType.UNDEFINED_DATE]: encodeUndefinedDate,
  [MetadatumType.TEXT]: identity,
  [MetadatumType.LINK]: identity,
  [MetadatumType.RELATED_DATA]: identity,
  [MetadatumType.EXTERNAL_DATA_NOTE]: identity,
  [MetadatumType.APPENDIX]: identity,
  [MetadatumType.FOOTNOTE_REFERENCE]: identity,
  [MetadatumType.FOOTNOTE_CHANGE]: identity,
  [MetadatumType.FOOTNOTE_COMMENT]: identity,
  [MetadatumType.FOOTNOTE_DECISION]: identity,
  [MetadatumType.FOOTNOTE_STATE_LAW]: identity,
  [MetadatumType.FOOTNOTE_EU_LAW]: identity,
  [MetadatumType.FOOTNOTE_OTHER]: identity,
  [MetadatumType.WORK_NOTE]: identity,
  [MetadatumType.DESCRIPTION]: identity,
  [MetadatumType.REFERENCE]: identity,
  [MetadatumType.ENTRY_INTO_FORCE_DATE_NOTE]: identity,
  [MetadatumType.PROOF_INDICATION]: encodeProofIndication,
  [MetadatumType.PROOF_TYPE]: encodeProofType,
  [MetadatumType.OTHER_TYPE]: encodeOtherType,
  [MetadatumType.NOTE]: identity,
  [MetadatumType.ARTICLE]: identity,
  [MetadatumType.OFFICIAL_LONG_TITLE]: identity,
  [MetadatumType.RIS_ABBREVIATION]: identity,
  [MetadatumType.DOCUMENT_NUMBER]: identity,
  [MetadatumType.DOCUMENT_CATEGORY]: identity,
  [MetadatumType.OFFICIAL_SHORT_TITLE]: identity,
  [MetadatumType.OFFICIAL_ABBREVIATION]: identity,
  [MetadatumType.COMPLETE_CITATION]: identity,
  [MetadatumType.CELEX_NUMBER]: identity,
  [MetadatumType.TIME]: identity,
}

/**
 * Decodes a list of metadata from the API response schema to the domain format. It
 * does that by grouping and mapping them by their type, sort them by order in
 * each group and finally map them to their decoded value property based on the
 * respective datum type.
 *
 * @example
 * ```ts
 * decodeMetadata([
 *  { type: COMMENT, value: "text", order: 2 }
 *  { type: DATE, value: "20-03-2001", order: 1 }
 *  { type: COMMENT, value: "other text", order: 1 }
 * ])
 * // => { COMMENT: ["other text", "text"], DATE: ["20-03-2001"] }
 * ```
 *
 * @param metadata to decode from response schema
 * @returns grouped, mapped, sorted metadata values
 */
function decodeMetadata(metadata: MetadatumSchema[]): Metadata {
  const grouped = groupBy(metadata, (datum) => datum.type)
  return mapValues(grouped, (data) => {
    const sortedData = data.sort(compareOrder)
    return sortedData.map((datum) => DECODERS[datum.type](datum.value))
  })
}

/**
 * Encodes some metadata from the domain format to the API request schema. The
 * mapping keys are used as type, also used for the encoding of the values. The
 * index within a group is used as order property. Finally, all groups get merged
 * back together.
 *
 * @example
 * ```ts
 * encodeMetadata({ COMMENT: ["other text", "text"], DATE: ["20-03-2001"] })
 * // =>
 * // [
 * //   { type: COMMENT, value: "other text", order: 1 },
 * //   { type: COMMENT, value: "text", order: 2 },
 * //   { type: DATE, value: "20-03-2001, order: 1 },
 * // ]
 * ```
 *
 * @param metadata to encode from response schema
 * @returns un-grouped metadata object collection
 */
function encodeMetadata(metadata: Metadata): MetadatumSchema[] | null {
  if (Object.entries(metadata).length == 0) {
    return null
  }

  const encodedMapping = mapValues(metadata, (group, type) =>
    group?.map((value, index) => ({
      type,
      order: index + 1,
      value: ENCODERS[type](value as never),
    }))
  )

  return mergeValues(encodedMapping)
}

/**
 * Decodes a list of metadata sections from the API response schema to take into account the extra layer of sections
 * created in the frontend to handle footnotes and the order of the repeated metadata inside each of the sections.
 * the function only modifies the footnote sections and return a list of MetadataSectionSchema.
 *
 * @example
 * ```ts
 * decodeFootnotesSections([
 *  {
 *    name: FOOTNOTES,
 *    order: 1,
 *    metadata: [
 *       { type: FOOTNOTE_REFERENCE, value: "1", order: 1 },
 *       { type: FOOTNOTE_COMMENT, value: "comment 1", order: 1 },
 *     ],
 *  },
 *  {
 *    name: RELEASE:
 *    order: 1,
 *  },
 *  {
 *    name: FOOTNOTES,
 *    order: 2,
 *    metadata: [
 *      { type: FOOTNOTE_COMMENT, value: "comment 2", order: 1 },
 *      { type: FOOTNOTE_COMMENT, value: "comment 3", order: 1 },
 *    ]
 *  }
 * ])
 * =====>
 * [
 *  {
 *    name: FOOTNOTES,
 *    order: 1,
 *    metadata: undefined,
 *    sections: [
 *        {
 *          name: FOOTNOTE,
 *          order: 1,
 *          metadata: [
 *              { type: FOOTNOTE_REFERENCE, value: "1", order: 1 },
 *              { type: FOOTNOTE_COMMENT, value: "comment 1", order: 1 },
 *          ]
 *        },
 *    ]
 *  },
 *  {
 *    name: RELEASE:
 *    order: 1,
 *  },
 *  {
 *    name: FOOTNOTES,
 *    order: 2,
 *    metadata: undefined,
 *    sections: [
 *        {
 *          name: FOOTNOTE,
 *          order: 1,
 *          metadata: [
 *              { type: FOOTNOTE_COMMENT, value: "comment 2", order: 1 },
 *              { type: FOOTNOTE_COMMENT, value: "comment 3", order: 1 },
 *          ]
 *        },
 *    ]
 *  },
 * ]
 * ```
 *
 * @param sections to decode from response schema
 * @returns modified sections including footnote sections metadata sections
 */

function decodeFootnotesSections(
  sections: MetadataSectionSchema[]
): MetadataSectionSchema[] {
  return sections.map(
    (section: MetadataSectionSchema): MetadataSectionSchema => {
      return section.name != MetadataSectionName.FOOTNOTES
        ? section
        : {
            name: section.name,
            order: section.order,
            metadata: null,
            sections: section.metadata?.map(
              (metadata: MetadatumSchema): MetadataSectionSchema => ({
                name: MetadataSectionName.FOOTNOTE,
                order: metadata.order,
                metadata: [metadata],
                sections: null,
              })
            ),
          }
    }
  )
}

/**
 * Encode a list of metadata sections from the domain to take into account the extra layer of sections
 * created in the frontend to handle footnotes and the order of the repeated metadata inside each of the sections.
 * the function only modifies the footnote sections and return a list of MetadataSectionSchema.
 *
 * @example
 * ```ts
 * encodeFootnotesSections([
 *  {
 *    name: FOOTNOTES,
 *    order: 1,
 *    metadata: undefined,
 *    sections: [
 *        {
 *          name: FOOTNOTE,
 *          order: 1,
 *          metadata: [
 *              { type: FOOTNOTE_REFERENCE, value: "1", order: 1 },
 *              { type: FOOTNOTE_COMMENT, value: "comment 1", order: 1 },
 *          ]
 *        },
 *    ]
 *  },
 *  {
 *    name: RELEASE:
 *    order: 1,
 *  },
 *  {
 *    name: FOOTNOTES,
 *    order: 2,
 *    metadata: undefined,
 *    sections: [
 *        {
 *          name: FOOTNOTE,
 *          order: 1,
 *          metadata: [
 *              { type: FOOTNOTE_COMMENT, value: "comment 2", order: 1 },
 *              { type: FOOTNOTE_COMMENT, value: "comment 3", order: 1 },
 *          ]
 *        },
 *    ]
 *  },
 * ])
 * =====>
 * [
 *  {
 *    name: FOOTNOTES,
 *    order: 1,
 *    metadata: [
 *       { type: FOOTNOTE_REFERENCE, value: "1", order: 1 },
 *       { type: FOOTNOTE_COMMENT, value: "comment 1", order: 1 },
 *     ],
 *  },
 *  {
 *    name: RELEASE:
 *    order: 1,
 *  },
 *  {
 *    name: FOOTNOTES,
 *    order: 2,
 *    metadata: [
 *      { type: FOOTNOTE_COMMENT, value: "comment 2", order: 1 },
 *      { type: FOOTNOTE_COMMENT, value: "comment 3", order: 1 },
 *    ]
 *  }
 * ]
 * ```
 *
 * @param sections to decode from response schema
 * @returns modified sections including footnote sections metadata sections
 */

function encodeFootnotesSections(
  sections: MetadataSectionSchema[]
): MetadataSectionSchema[] {
  return sections.map(
    (section: MetadataSectionSchema): MetadataSectionSchema => {
      return section.name != MetadataSectionName.FOOTNOTES
        ? section
        : {
            name: section.name,
            order: section.order,
            metadata: section.sections
              ? section.sections.reduce(
                  (
                    result: MetadatumSchema[],
                    section: MetadataSectionSchema,
                    index
                  ) => {
                    const footnote = section?.metadata?.at(0)
                    if (footnote != undefined) {
                      result.push({
                        order: index + 1,
                        value: footnote?.value,
                        type: footnote?.type,
                      })
                    }
                    return result
                  },
                  []
                )
              : null,
            sections: null,
          }
    }
  )
}

/**
 * Encodes some metadata sections from the domain format to the API request
 * schema. The mapping keys are used as name. The keys within the sections are
 * then separated by datum and section types and names to recursively encode the
 * section. The index within a group is used as order property. Finally all
 * groups are merged back together.
 *
 * @example
 * ```ts
 * encodeMetadataSection({
 *   RELEASE: [
 *     { _ },
 *     { DATE: ["20-03-2001"], COMMENT: ["text"], INSTITUTION: [{ NAME: ["House"] }]},
 *   ],
 *   PROOF: [
 *     { DOCUMENT: ["example.pdf", "other.txt"] },
 *    ],
 * })
 * // [
 * //   {
 * //     name: RELEASE:
 * //     order: 1,
 * //   },
 * //   {
 * //     name: RELEASE,
 * //      order: 2,
 * //      metadata: [
 * //        { type: DATE, value: "20-03-2001", order: 1 },
 * //        { type: COMMENT, value: "text", order: 1 },
 * //      ],
 * //      sections: [{
 * //        name: INSTITUTION,
 * //        order: 1,
 * //        metadata: [
 * //          { type: NAME, value: "House", order: 1 }
 * //        ],
 * //      }],
 * //   },
 * //   {
 * //     name: PROOF,
 * //     order: 1,
 * //     metadata: [
 * //      { type: DOCUMENT, value: "example.pdf", order: 1 }
 * //      { type: DOCUMENT, value: "other.txt", order: 2 }
 * //     ]
 * //   }
 * // ]
 * ```
 *
 * @param sections to encode as request schema
 * @returns un-grouped metadata section object collection
 */
export function encodeMetadataSections(
  sections: MetadataSections
): MetadataSectionSchema[] | null {
  if (Object.entries(sections).length == 0) {
    return null
  }
  const encodedMapping = mapValues(sections, (group, name) =>
    group?.map((value, index) => {
      const metadata = filterEntries(
        value,
        (data, key) =>
          Object.keys(MetadatumType).includes(key) && data !== undefined
      ) as Metadata

      const childSections = filterEntries(
        value,
        (data, key) =>
          Object.keys(MetadataSectionName).includes(key) && data !== undefined
      ) as MetadataSections

      return {
        name,
        order: index + 1,
        metadata: encodeMetadata(metadata),
        sections: encodeMetadataSections(childSections),
      }
    })
  )

  const encodedSections = mergeValues(encodedMapping)
  const nonEmptySections = encodeFootnotesSections(encodedSections).filter(
    (section) =>
      (section.metadata && section.metadata.length > 0) ||
      (section.sections && section.sections.length > 0)
  )
  return nonEmptySections
}

/**
 * Decodes a list of metadata sections from the API response schema to the
 * domain format. It does that by grouping and mapping them by their name,
 * sort them by order in each group and finally map them to their recursively
 * decoded sections and metadata.
 *
 * @example
 * ```ts
 * decodeMetadataSections([
 *  {
 *    name: RELEASE,
 *     order: 2,
 *     metadata: [
 *       { type: DATE, value: "20-03-2001", order: 1 },
 *       { type: COMMENT, value: "text", order: 1 },
 *     ],
 *     sections: [{
 *       name: INSTITUTION,
 *       order: 1,
 *       metadata: [
 *         { type: NAME, value: "House", order: 1 }
 *       ],
 *     }],
 *  },
 *  {
 *    name: RELEASE:
 *    order: 1,
 *  },
 *  {
 *    name: PROOF,
 *    order: 1,
 *    metadata: [
 *     { type: DOCUMENT, value: "example.pdf", order: 1 }
 *     { type: DOCUMENT, value: "other.txt", order: 2 }
 *    ]
 *  }
 * ])
 * // =>
 * // {
 * //   RELEASE: [
 * //     { _ },
 * //     { DATE: ["20-03-2001"], COMMENT: ["text"], INSTITUTION: [{ NAME: ["House"] }]},
 * //   ],
 * //   PROOF: [
 * //     { DOCUMENT: ["example.pdf", "other.txt"] },
 * //   ],
 * // }
 * ```
 *
 * @param sections to decode from response schema
 * @returns grouped, mapped, sorted metadata sections
 */
export function decodeMetadataSections(
  sections: MetadataSectionSchema[]
): MetadataSections {
  const modifiedSections = decodeFootnotesSections(sections)
  const grouped = groupBy(modifiedSections, (section) => section.name)
  return mapValues(grouped, (sections) =>
    sections.sort(compareOrder).map((section) => ({
      ...decodeMetadata(section.metadata ?? []),
      ...decodeMetadataSections(section.sections ?? []),
    }))
  )
}

export function decodeNorm(norm: NormResponseSchema): Norm {
  const { metadataSections, ...stableData } = norm
  return {
    metadataSections: decodeMetadataSections(metadataSections ?? []),
    ...stableData,
  }
}

export function encodeFlatMetadata(
  flatMetadata: FlatMetadata
): FlatMetadataRequestSchema {
  return {
    eli: encodeString(flatMetadata.eli),
  }
}
