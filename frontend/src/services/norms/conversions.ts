import { MetadatumSchema, MetadataSectionSchema } from "./schemas"
import {
  groupBy,
  mapValues,
  compareOrder,
  mergeValues,
  filterEntries,
} from "./utilities"

import {
  MetadatumType,
  MetadataSectionName,
  Metadata,
  MetadataSections,
  MetadataValueType,
} from "@/domain/Norm"

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

const DECODERS: MetadataValueDecoders = {
  [MetadatumType.KEYWORD]: (data: string) => data,
  [MetadatumType.UNOFFICIAL_LONG_TITLE]: (data: string) => data,
  [MetadatumType.UNOFFICIAL_SHORT_TITLE]: (data: string) => data,
  [MetadatumType.UNOFFICIAL_ABBREVIATION]: (data: string) => data,
  [MetadatumType.UNOFFICIAL_REFERENCE]: (data: string) => data,
  [MetadatumType.DIVERGENT_DOCUMENT_NUMBER]: (data: string) => data,
  [MetadatumType.REFERENCE_NUMBER]: (data: string) => data,
  [MetadatumType.DEFINITION]: (data: string) => data,
  [MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW]: (data: string) => data,
  [MetadatumType.AGE_OF_MAJORITY_INDICATION]: (data: string) => data,
  [MetadatumType.VALIDITY_RULE]: (data: string) => data,
  [MetadatumType.LEAD_JURISDICTION]: (data: string) => data,
  [MetadatumType.LEAD_UNIT]: (data: string) => data,
  [MetadatumType.PARTICIPATION_TYPE]: (data: string) => data,
  [MetadatumType.PARTICIPATION_INSTITUTION]: (data: string) => data,
  [MetadatumType.SUBJECT_FNA]: (data: string) => data,
  [MetadatumType.SUBJECT_PREVIOUS_FNA]: (data: string) => data,
  [MetadatumType.SUBJECT_GESTA]: (data: string) => data,
  [MetadatumType.SUBJECT_BGB_3]: (data: string) => data,
}

const ENCORDERS: MetadataValueEncoders = {
  [MetadatumType.KEYWORD]: (data: string) => data,
  [MetadatumType.UNOFFICIAL_LONG_TITLE]: (data: string) => data,
  [MetadatumType.UNOFFICIAL_SHORT_TITLE]: (data: string) => data,
  [MetadatumType.UNOFFICIAL_ABBREVIATION]: (data: string) => data,
  [MetadatumType.UNOFFICIAL_REFERENCE]: (data: string) => data,
  [MetadatumType.DIVERGENT_DOCUMENT_NUMBER]: (data: string) => data,
  [MetadatumType.REFERENCE_NUMBER]: (data: string) => data,
  [MetadatumType.DEFINITION]: (data: string) => data,
  [MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW]: (data: string) => data,
  [MetadatumType.AGE_OF_MAJORITY_INDICATION]: (data: string) => data,
  [MetadatumType.VALIDITY_RULE]: (data: string) => data,
  [MetadatumType.LEAD_JURISDICTION]: (data: string) => data,
  [MetadatumType.LEAD_UNIT]: (data: string) => data,
  [MetadatumType.PARTICIPATION_TYPE]: (data: string) => data,
  [MetadatumType.PARTICIPATION_INSTITUTION]: (data: string) => data,
  [MetadatumType.SUBJECT_FNA]: (data: string) => data,
  [MetadatumType.SUBJECT_PREVIOUS_FNA]: (data: string) => data,
  [MetadatumType.SUBJECT_GESTA]: (data: string) => data,
  [MetadatumType.SUBJECT_BGB_3]: (data: string) => data,
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
 *  { type: COMMENT, value: "text", order: 1 }
 *  { type: DATE, value: "20-03-2001", order: 0 }
 *  { type: COMMENT, value: "other text", order: 0 }
 * ])
 * // => { COMMENT: ["other text", "text"], DATE: ["20-03-2001"] }
 * ```
 *
 * @param metadata to decode from response schema
 * @returns grouped, mapped, sorted metadata values
 */
function decodeMetadata(metadata: MetadatumSchema[]): Metadata {
  const grouped = groupBy(metadata, (datum) => datum.type)
  return mapValues(grouped, (data) =>
    data.sort(compareOrder).map((datum) => DECODERS[datum.type](datum.value))
  )
}

/**
 * Encodes some metadata from the domain format to the API request schema. The
 * mapping keys are used as type, also used for the encoding of the values. The
 * index within a group is used as order property. Finally all groups get merged
 * back together.
 *
 * @example
 * ```ts
 * encodeMetadata({ COMMENT: ["other text", "text"], DATE: ["20-03-2001"] })
 * // =>
 * // [
 * //   { type: COMMENT, value: "other text", order: 0 },
 * //   { type: COMMENT, value: "text", order: 1 },
 * //   { type: DATE, value: "20-03-2001, order: 0 },
 * // ]
 * ```
 *
 * @param metadata to encode from response schema
 * @returns un-grouped metadata object collection
 */
function encodeMetadata(metadata: Metadata): MetadatumSchema[] {
  const encodedMapping = mapValues(metadata, (group, type) =>
    group?.map((value, order) => ({
      type,
      order,
      // FIXME: Type system is tricky here...
      value: ENCORDERS[type](value as never),
    }))
  )

  return mergeValues(encodedMapping)
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
 *     order: 1,
 *     metadata: [
 *       { type: DATE, value: "20-03-2001", order: 0 },
 *       { type: COMMENT, value: "text", order: 0 },
 *     ],
 *     sections: [{
 *       name: INSTITUTION,
 *       order: 0,
 *       metadata: [
 *         { type: NAME, value: "House", order: 0 }
 *       ],
 *     }],
 *  },
 *  {
 *    name: RELEASE:
 *    order: 0,
 *  },
 *  {
 *    name: PROOF,
 *    order: 0,
 *    metadata: [
 *     { type: DOCUMENT, value: "example.pdf", order: 0 }
 *     { type: DOCUMENT, value: "other.txt", order: 1 }
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
  const grouped = groupBy(sections, (section) => section.name)
  return mapValues(grouped, (sections) =>
    sections.sort(compareOrder).map((section) => ({
      ...decodeMetadata(section.metadata ?? []),
      ...decodeMetadataSections(section.sections ?? []),
    }))
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
 * //     order: 0,
 * //   },
 * //   {
 * //     name: RELEASE,
 * //      order: 1,
 * //      metadata: [
 * //        { type: DATE, value: "20-03-2001", order: 0 },
 * //        { type: COMMENT, value: "text", order: 0 },
 * //      ],
 * //      sections: [{
 * //        name: INSTITUTION,
 * //        order: 0,
 * //        metadata: [
 * //          { type: NAME, value: "House", order: 0 }
 * //        ],
 * //      }],
 * //   },
 * //   {
 * //     name: PROOF,
 * //     order: 0,
 * //     metadata: [
 * //      { type: DOCUMENT, value: "example.pdf", order: 0 }
 * //      { type: DOCUMENT, value: "other.txt", order: 1 }
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
    group?.map((value, order) => {
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
        order,
        metadata: encodeMetadata(metadata),
        sections: encodeMetadataSections(childSections),
      }
    })
  )

  return mergeValues(encodedMapping)
}
