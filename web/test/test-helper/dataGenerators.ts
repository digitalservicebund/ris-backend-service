import { InputType } from "@/domain"
import type {
  BaseInputAttributes,
  BaseInputField,
  TextInputAttributes,
  TextInputField,
} from "@/domain"
import { Article, Norm, Paragraph } from "@/domain/Norm"

const ALPHABET_CHARACTERS = "abcdefghijklmnopqrstuvwxyz"
const HEXADECIMAL_CHARACTERS = "0123456789abcdef"

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
      Math.floor(Math.random() * characterSet.length)
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

export function generateNorm(partialNorm?: Partial<Norm>): Norm {
  return {
    guid: generateGuid(),
    articles: [generateArticle()],
    officialLongTitle: generateString(),
    officialShortTitle: generateString(),
    officialAbbreviation: generateString(),
    referenceNumber: generateString(),
    publicationDate: generateString(),
    announcementDate: generateString(),
    citationDate: generateString(),
    frameKeywords: generateString(),
    providerEntity: generateString(),
    providerDecidingBody: generateString(),
    providerIsResolutionMajority: false,
    leadJurisdiction: generateString(),
    leadUnit: generateString(),
    participationType: generateString(),
    participationInstitution: generateString(),
    documentTypeName: generateString(),
    documentNormCategory: generateString(),
    documentTemplateName: generateString(),
    subjectFna: generateString(),
    subjectPreviousFna: generateString(),
    subjectGesta: generateString(),
    subjectBgb3: generateString(),
    unofficialLongTitle: generateString(),
    unofficialShortTitle: generateString(),
    unofficialAbbreviation: generateString(),
    risAbbreviation: generateString(),
    ...partialNorm,
  }
}
