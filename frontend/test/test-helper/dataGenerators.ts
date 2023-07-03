import {
  Article,
  FlatMetadata,
  Metadata,
  MetadataSectionName,
  MetadataSections,
  MetadataValueType,
  MetadatumType,
  Norm,
  NormCategory,
  OtherType,
  Paragraph,
  ProofIndication,
  ProofType,
  UndefinedDate,
} from "@/domain/Norm"
import {
  InputType,
  BaseInputAttributes,
  BaseInputField,
  TextInputAttributes,
  TextInputField,
} from "@/shared/components/input/types"

type MetadataValueGenerators = {
  [Type in keyof MetadataValueType]: () => MetadataValueType[Type]
}

const METADATA_VALUE_GENERATORS: MetadataValueGenerators = {
  [MetadatumType.KEYWORD]: generateString,
  [MetadatumType.UNOFFICIAL_LONG_TITLE]: generateString,
  [MetadatumType.UNOFFICIAL_SHORT_TITLE]: generateString,
  [MetadatumType.UNOFFICIAL_ABBREVIATION]: generateString,
  [MetadatumType.UNOFFICIAL_REFERENCE]: generateString,
  [MetadatumType.DIVERGENT_DOCUMENT_NUMBER]: generateString,
  [MetadatumType.REFERENCE_NUMBER]: generateString,
  [MetadatumType.DEFINITION]: generateString,
  [MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW]: generateString,
  [MetadatumType.AGE_OF_MAJORITY_INDICATION]: generateString,
  [MetadatumType.VALIDITY_RULE]: generateString,
  [MetadatumType.LEAD_JURISDICTION]: generateString,
  [MetadatumType.LEAD_UNIT]: generateString,
  [MetadatumType.PARTICIPATION_TYPE]: generateString,
  [MetadatumType.PARTICIPATION_INSTITUTION]: generateString,
  [MetadatumType.SUBJECT_FNA]: generateString,
  [MetadatumType.SUBJECT_PREVIOUS_FNA]: generateString,
  [MetadatumType.SUBJECT_GESTA]: generateString,
  [MetadatumType.SUBJECT_BGB_3]: generateString,
  [MetadatumType.DATE]: generateString,
  [MetadatumType.YEAR]: generateString,
  [MetadatumType.RANGE_START]: generateString,
  [MetadatumType.RANGE_END]: generateString,
  [MetadatumType.ANNOUNCEMENT_MEDIUM]: generateString,
  [MetadatumType.ANNOUNCEMENT_GAZETTE]: generateString,
  [MetadatumType.ADDITIONAL_INFO]: generateString,
  [MetadatumType.EXPLANATION]: generateString,
  [MetadatumType.AREA_OF_PUBLICATION]: generateString,
  [MetadatumType.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA]:
    generateString,
  [MetadatumType.EU_GOVERNMENT_GAZETTE]: generateString,
  [MetadatumType.SERIES]: generateString,
  [MetadatumType.OTHER_OFFICIAL_REFERENCE]: generateString,
  [MetadatumType.PAGE]: generateString,
  [MetadatumType.NUMBER]: generateString,
  [MetadatumType.EDITION]: generateString,
  [MetadatumType.ENTITY]: generateString,
  [MetadatumType.DECIDING_BODY]: generateString,
  [MetadatumType.RESOLUTION_MAJORITY]: pickRandomBoolean,
  [MetadatumType.TYPE_NAME]: generateString,
  [MetadatumType.NORM_CATEGORY]: pickRandomNormCategory,
  [MetadatumType.TEMPLATE_NAME]: generateString,
  [MetadatumType.UNDEFINED_DATE]: pickRandomUndefinedDate,
  [MetadatumType.TEXT]: generateString,
  [MetadatumType.LINK]: generateString,
  [MetadatumType.RELATED_DATA]: generateString,
  [MetadatumType.EXTERNAL_DATA_NOTE]: generateString,
  [MetadatumType.APPENDIX]: generateString,
  [MetadatumType.FOOTNOTE_REFERENCE]: generateString,
  [MetadatumType.FOOTNOTE_CHANGE]: generateString,
  [MetadatumType.FOOTNOTE_COMMENT]: generateString,
  [MetadatumType.FOOTNOTE_DECISION]: generateString,
  [MetadatumType.FOOTNOTE_STATE_LAW]: generateString,
  [MetadatumType.FOOTNOTE_EU_LAW]: generateString,
  [MetadatumType.FOOTNOTE_OTHER]: generateString,
  [MetadatumType.WORK_NOTE]: generateString,
  [MetadatumType.DESCRIPTION]: generateString,
  [MetadatumType.REFERENCE]: generateString,
  [MetadatumType.ENTRY_INTO_FORCE_DATE_NOTE]: generateString,
  [MetadatumType.PROOF_INDICATION]: pickRandomProofIndication,
  [MetadatumType.PROOF_TYPE]: pickRandomProofType,
  [MetadatumType.OTHER_TYPE]: pickRandomOtherType,
  [MetadatumType.NOTE]: generateString,
  [MetadatumType.ARTICLE]: generateString,
  [MetadatumType.OFFICIAL_LONG_TITLE]: generateString,
  [MetadatumType.RIS_ABBREVIATION]: generateString,
  [MetadatumType.DOCUMENT_NUMBER]: generateString,
  [MetadatumType.DOCUMENT_CATEGORY]: generateString,
  [MetadatumType.OFFICIAL_SHORT_TITLE]: generateString,
  [MetadatumType.OFFICIAL_ABBREVIATION]: generateString,
  [MetadatumType.COMPLETE_CITATION]: generateString,
  [MetadatumType.CELEX_NUMBER]: generateString,
  [MetadatumType.TIME]: generateString,
}

const ALPHABET_CHARACTERS = "abcdefghijklmnopqrstuvwxyz"
const HEXADECIMAL_CHARACTERS = "0123456789abcdef"

export function generateRandomNumber(minimum = 0, maximum = 10): number {
  return Math.floor(Math.random() * (maximum - minimum) + minimum)
}

export function pickRandomBoolean(): boolean {
  return Math.random() < 0.5
}

export function generateString(options?: {
  characterSet?: string
  length?: number
  prefix?: string
}): string {
  const characterSet = options?.characterSet ?? ALPHABET_CHARACTERS
  const length = options?.length ?? 5
  let output = options?.prefix ?? ""

  for (let i = 0; i < length; i++) {
    output += characterSet.charAt(
      generateRandomNumber(0, characterSet.length - 1)
    )
  }

  return output
}

export function generateBaseInputAttributes(
  partialAttributes?: Partial<BaseInputAttributes>
): BaseInputAttributes {
  return {
    ariaLabel: generateString({ prefix: "aria-label-" }),
    ...partialAttributes,
  }
}

export function generateTextInputAttributes(
  partialAttributes?: Partial<TextInputAttributes>
): TextInputAttributes {
  return {
    ...generateBaseInputAttributes(),
    placeholder: undefined,
    ...partialAttributes,
  }
}

export function generateBaseInputField(
  partialField?: Partial<BaseInputField>
): BaseInputField {
  return {
    type: InputType.TEXT,
    name: generateString({ prefix: "name-" }),
    label: generateString({ prefix: "Label " }),
    inputAttributes: generateBaseInputAttributes(),
    ...partialField,
  }
}

export function generateTextInputField(
  partialField?: Partial<TextInputField>
): TextInputField {
  return {
    ...generateBaseInputField(),
    type: InputType.TEXT,
    inputAttributes: generateTextInputAttributes(),
    ...partialField,
  }
}
export function generateGuid(): string {
  const first = generateString({
    length: 8,
    characterSet: HEXADECIMAL_CHARACTERS,
  })
  const second = generateString({
    length: 4,
    characterSet: HEXADECIMAL_CHARACTERS,
  })
  const third = generateString({
    length: 4,
    characterSet: HEXADECIMAL_CHARACTERS,
  })
  const fourth = generateString({
    length: 4,
    characterSet: HEXADECIMAL_CHARACTERS,
  })
  const fith = generateString({
    length: 12,
    characterSet: HEXADECIMAL_CHARACTERS,
  })
  return `${first}-${second}-${third}-${fourth}-${fith}`
}

export function generateParagraph(
  partialParagraph?: Partial<Paragraph>
): Paragraph {
  return {
    guid: generateGuid(),
    marker: generateString({ prefix: "marker " }),
    text: generateString({ prefix: "text", length: 40 }),
    ...partialParagraph,
  }
}

export function generateArticle(partialArticle?: Partial<Article>): Article {
  return {
    guid: generateGuid(),
    title: generateString({ prefix: "title " }),
    marker: generateString({ prefix: "marker " }),
    paragraphs: [generateParagraph()],
    ...partialArticle,
  }
}

export function pickRandomProofIndication(): ProofIndication {
  const options = Object.values(ProofIndication)
  const index = generateRandomNumber(0, options.length - 1)
  return options[index]
}

export function pickRandomProofType(): ProofType {
  const options = Object.values(ProofType)
  const index = generateRandomNumber(0, options.length - 1)
  return options[index]
}

export function pickRandomOtherType(): OtherType {
  const options = Object.values(OtherType)
  const index = generateRandomNumber(0, options.length - 1)
  return options[index]
}
export function pickRandomNormCategory(): NormCategory {
  const options = Object.values(NormCategory)
  const index = generateRandomNumber(0, options.length - 1)
  return options[index]
}

export function pickRandomUndefinedDate(): UndefinedDate {
  const options = Object.values(UndefinedDate)
  const index = generateRandomNumber(0, options.length - 1)
  return options[index]
}

export function pickRandomMetadatumType(): MetadatumType {
  const options = Object.values(MetadatumType)
  const index = generateRandomNumber(0, options.length - 1)
  return options[index]
}

export function generateMetadata(partialMetadata?: Partial<Metadata>) {
  const metadata = {} as Metadata
  const metadataCount = generateRandomNumber()
  for (let i = 0; i < metadataCount; i++) {
    const type = pickRandomMetadatumType()
    const values = new Array(generateRandomNumber())
      .fill(0)
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore TS2345
      .map(METADATA_VALUE_GENERATORS[type]) as string[] &
      boolean[] &
      NormCategory[] &
      UndefinedDate[] &
      ProofIndication[] &
      ProofType[] &
      OtherType[]
    metadata[type] = values
  }

  Object.entries(partialMetadata ?? {}).forEach(([type, values]) => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    metadata[type as MetadatumType] = values as any
  })

  return metadata
}

export function pickRandomMetadataSectionName(): MetadataSectionName {
  const options = Object.values(MetadataSectionName)
  const index = generateRandomNumber(0, options.length - 1)
  return options[index]
}

export function generateMetadataSections(
  partialSections?: Partial<MetadataSections>
): MetadataSections {
  const sections = {} as MetadataSections

  for (let i = 0; i < generateRandomNumber(1); i++) {
    const name = pickRandomMetadataSectionName()
    // Do not use child sections here to prevent random infinite loop.
    sections[name] = new Array(generateRandomNumber())
      .fill(0)
      .map(generateMetadata)
  }

  Object.entries(partialSections ?? {}).forEach(([name, metadata]) => {
    sections[name as MetadataSectionName] = metadata
  })

  return sections
}

export function generateFlatMetadata(
  partialFlatMetadata?: Partial<FlatMetadata>
): FlatMetadata {
  return {
    announcementDate: generateString(),
    eli: generateString(),
    ...partialFlatMetadata,
  }
}

export function generateNorm(partialNorm?: Partial<Norm>): Norm {
  return {
    guid: generateGuid(),
    articles: [generateArticle()],
    metadataSections: generateMetadataSections(),
    ...generateFlatMetadata(),
    ...partialNorm,
  }
}
