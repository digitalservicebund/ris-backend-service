type NullableType<Type> = {
  [Property in keyof Type]: Type[Property] | null
}

export type Article = {
  guid: string
  title: string
  marker: string
  readonly paragraphs: Paragraph[]
}

export type Paragraph = {
  guid: string
  marker: string
  text: string
}

export type NullableFrameData = NullableType<FrameData>

export type NullableNormEditRequest = NullableType<NormEditRequest>

export type NormBase = {
  documentTemplateName?: string
  leadUnit?: string
  participationInstitution?: string
  subjectBgb3?: string
  ageIndicationEnd?: string
  ageIndicationStart?: string
  announcementDate?: string
  applicationScopeArea?: string
  applicationScopeEndDate?: string
  applicationScopeStartDate?: string
  categorizedReference?: string
  celexNumber?: string
  citationDate?: string
  citationYear?: string
  completeCitation?: string
  digitalAnnouncementDate?: string
  digitalAnnouncementArea?: string
  digitalAnnouncementAreaNumber?: string
  digitalAnnouncementEdition?: string
  digitalAnnouncementExplanations?: string
  digitalAnnouncementInfo?: string
  digitalAnnouncementMedium?: string
  digitalAnnouncementPage?: string
  digitalAnnouncementYear?: string
  digitalEvidenceAppendix?: string
  digitalEvidenceExternalDataNote?: string
  digitalEvidenceLink?: string
  digitalEvidenceRelatedData?: string
  divergentEntryIntoForceDate?: string
  divergentEntryIntoForceDateState?: string
  divergentExpirationDate?: string
  divergentExpirationDateState?: string
  documentCategory?: string
  documentNormCategory?: string
  documentNumber?: string
  documentStatusDate?: string
  documentStatusDescription?: string
  documentStatusEntryIntoForceDate?: string
  documentStatusProof?: string
  documentStatusReference?: string
  documentStatusWorkNote?: string
  documentTextProof?: string
  documentTypeName?: string
  entryIntoForceDate?: string
  entryIntoForceDateState?: string
  entryIntoForceNormCategory?: string
  euAnnouncementExplanations?: string
  euAnnouncementGazette?: string
  euAnnouncementInfo?: string
  euAnnouncementNumber?: string
  euAnnouncementPage?: string
  euAnnouncementSeries?: string
  euAnnouncementYear?: string
  eli?: string
  expirationDate?: string
  expirationDateState?: string
  expirationNormCategory?: string
  isExpirationDateTemp?: boolean
  leadJurisdiction?: string
  officialAbbreviation?: string
  officialLongTitle: string
  officialShortTitle?: string
  otherDocumentNote?: string
  otherFootnote?: string
  footnoteChange?: string
  footnoteComment?: string
  footnoteDecision?: string
  footnoteStateLaw?: string
  footnoteEuLaw?: string
  otherOfficialAnnouncement?: string
  otherStatusNote?: string
  participationType?: string
  principleEntryIntoForceDate?: string
  principleEntryIntoForceDateState?: string
  principleExpirationDate?: string
  principleExpirationDateState?: string
  printAnnouncementExplanations?: string
  printAnnouncementGazette?: string
  printAnnouncementInfo?: string
  printAnnouncementNumber?: string
  printAnnouncementPage?: string
  printAnnouncementYear?: string
  providerEntity?: string
  providerDecidingBody?: string
  providerIsResolutionMajority?: boolean
  publicationDate?: string
  reissueArticle?: string
  reissueDate?: string
  reissueNote?: string
  reissueReference?: string
  repealArticle?: string
  repealDate?: string
  repealNote?: string
  repealReferences?: string
  risAbbreviation?: string
  statusDate?: string
  statusDescription?: string
  statusNote?: string
  statusReference?: string
  subjectFna?: string
  subjectGesta?: string
  subjectPreviousFna?: string
  text?: string
}

export type FrameData = NormBase & {
  frameKeywords?: string[]
  validityRule?: string[]
  unofficialShortTitle?: string[]
  unofficialReference?: string[]
  unofficialLongTitle?: string[]
  unofficialAbbreviation?: string[]
  risAbbreviationInternationalLaw?: string[]
  referenceNumber?: string[]
  definition?: string[]
  ageOfMajorityIndication?: string[]
  divergentDocumentNumber?: string[]
}

export interface Norm extends FrameData {
  readonly guid: string
  readonly articles: Article[]
  readonly files?: FileReference[]
}

export interface NormResponse extends NormBase {
  readonly guid: string
  readonly articles: Article[]
  readonly files?: FileReference[]
  metadata?: MetaDatum[]
}

export interface NormEditRequest extends NormBase {
  metadata?: MetaDatum[]
}

export interface FileReference {
  readonly name: string
  readonly hash: string
  readonly createdAt: string
}

export interface MetaDatum {
  readonly value: string
  readonly type: MetaDatumType
  readonly order: number
}

export enum MetaDatumType {
  KEYWORD = "KEYWORD",
  UNOFFICIAL_LONG_TITLE = "UNOFFICIAL_LONG_TITLE",
  UNOFFICIAL_SHORT_TITLE = "UNOFFICIAL_SHORT_TITLE",
  UNOFFICIAL_ABBREVIATION = "UNOFFICIAL_ABBREVIATION",
  UNOFFICIAL_REFERENCE = "UNOFFICIAL_REFERENCE",
  DIVERGENT_DOCUMENT_NUMBER = "DIVERGENT_DOCUMENT_NUMBER",
  REFERENCE_NUMBER = "REFERENCE_NUMBER",
  DEFINITION = "DEFINITION",
  RIS_ABBREVIATION_INTERNATIONAL_LAW = "RIS_ABBREVIATION_INTERNATIONAL_LAW",
  AGE_OF_MAJORITY_INDICATION = "AGE_OF_MAJORITY_INDICATION",
  VALIDITY_RULE = "VALIDITY_RULE",
}
