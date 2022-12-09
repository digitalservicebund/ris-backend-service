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
    documentTemplateName: generateString(),
    leadUnit: generateString(),
    participationInstitution: generateString(),
    subjectBgb3: generateString(),
    ageIndicationEnd: generateString(),
    ageIndicationStart: generateString(),
    ageOfMajorityIndication: generateString(),
    announcementDate: generateString(),
    applicationScopeArea: generateString(),
    applicationScopeEndDate: generateString(),
    applicationScopeStartDate: generateString(),
    categorizedReference: generateString(),
    celexNumber: generateString(),
    citationDate: generateString(),
    completeCitation: generateString(),
    definition: generateString(),
    digitalAnnouncementDate: generateString(),
    digitalAnnouncementArea: generateString(),
    digitalAnnouncementAreaNumber: generateString(),
    digitalAnnouncementEdition: generateString(),
    digitalAnnouncementExplanations: generateString(),
    digitalAnnouncementInfo: generateString(),
    digitalAnnouncementMedium: generateString(),
    digitalAnnouncementPage: generateString(),
    digitalAnnouncementYear: generateString(),
    digitalEvidenceAppendix: generateString(),
    digitalEvidenceExternalDataNote: generateString(),
    digitalEvidenceLink: generateString(),
    digitalEvidenceRelatedData: generateString(),
    divergentDocumentNumber: generateString(),
    divergentEntryIntoForceDate: generateString(),
    divergentEntryIntoForceDateState: generateString(),
    divergentExpirationDate: generateString(),
    divergentExpirationDateState: generateString(),
    documentCategory: generateString(),
    documentNormCategory: generateString(),
    documentNumber: generateString(),
    documentStatusDate: generateString(),
    documentStatusDescription: generateString(),
    documentStatusEntryIntoForceDate: generateString(),
    documentStatusProof: generateString(),
    documentStatusReference: generateString(),
    documentStatusWorkNote: generateString(),
    documentTextProof: generateString(),
    documentTypeName: generateString(),
    entryIntoForceDate: generateString(),
    entryIntoForceDateState: generateString(),
    euAnnouncementExplanations: generateString(),
    euAnnouncementGazette: generateString(),
    euAnnouncementInfo: generateString(),
    euAnnouncementNumber: generateString(),
    euAnnouncementPage: generateString(),
    euAnnouncementSeries: generateString(),
    euAnnouncementYear: generateString(),
    europeanLegalIdentifier: generateString(),
    expirationDate: generateString(),
    expirationDateState: generateString(),
    expirationNormCategory: generateString(),
    frameKeywords: generateString(),
    isExpirationDateTemp: false,
    leadJurisdiction: generateString(),
    officialAbbreviation: generateString(),
    officialLongTitle: generateString(),
    officialShortTitle: generateString(),
    otherDocumentNote: generateString(),
    otherFootnote: generateString(),
    otherOfficialAnnouncement: generateString(),
    otherStatusNote: generateString(),
    participationType: generateString(),
    principleEntryIntoForceDate: generateString(),
    principleEntryIntoForceDateState: generateString(),
    principleExpirationDate: generateString(),
    principleExpirationDateState: generateString(),
    printAnnouncementExplanations: generateString(),
    printAnnouncementGazette: generateString(),
    printAnnouncementInfo: generateString(),
    printAnnouncementNumber: generateString(),
    printAnnouncementPage: generateString(),
    printAnnouncementYear: generateString(),
    providerEntity: generateString(),
    providerDecidingBody: generateString(),
    providerIsResolutionMajority: false,
    publicationDate: generateString(),
    referenceNumber: generateString(),
    reissueArticle: generateString(),
    reissueDate: generateString(),
    reissueNote: generateString(),
    reissueReference: generateString(),
    repealArticle: generateString(),
    repealDate: generateString(),
    repealNote: generateString(),
    repealReferences: generateString(),
    risAbbreviation: generateString(),
    risAbbreviationInternationalLaw: generateString(),
    statusDate: generateString(),
    statusDescription: generateString(),
    statusNote: generateString(),
    statusReference: generateString(),
    subjectFna: generateString(),
    subjectGesta: generateString(),
    subjectPreviousFna: generateString(),
    text: generateString(),
    unofficialAbbreviation: generateString(),
    unofficialLongTitle: generateString(),
    unofficialReference: generateString(),
    unofficialShortTitle: generateString(),
    validityRule: generateString(),
    ...partialNorm,
  }
}
