export interface Norm extends NormBase, FlatMetadata {
  metadataSections?: MetadataSections
}

export interface NormBase {
  readonly guid: string
  readonly articles: Article[]
  readonly files?: FileReference[]
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

export interface FileReference {
  readonly name: string
  readonly hash: string
  readonly createdAt: string
}

export enum MetadatumType {
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
  LEAD_JURISDICTION = "LEAD_JURISDICTION",
  LEAD_UNIT = "LEAD_UNIT",
  PARTICIPATION_TYPE = "PARTICIPATION_TYPE",
  PARTICIPATION_INSTITUTION = "PARTICIPATION_INSTITUTION",
  SUBJECT_FNA = "SUBJECT_FNA",
  SUBJECT_PREVIOUS_FNA = "SUBJECT_PREVIOUS_FNA",
  SUBJECT_GESTA = "SUBJECT_GESTA",
  SUBJECT_BGB_3 = "SUBJECT_BGB_3",
}

// TODO: Establish typing that requires all `MetadatumType`s to be listed.
export type MetadataValueType = {
  [MetadatumType.KEYWORD]: string
  [MetadatumType.UNOFFICIAL_LONG_TITLE]: string
  [MetadatumType.UNOFFICIAL_SHORT_TITLE]: string
  [MetadatumType.UNOFFICIAL_ABBREVIATION]: string
  [MetadatumType.UNOFFICIAL_REFERENCE]: string
  [MetadatumType.DIVERGENT_DOCUMENT_NUMBER]: string
  [MetadatumType.REFERENCE_NUMBER]: string
  [MetadatumType.DEFINITION]: string
  [MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW]: string
  [MetadatumType.AGE_OF_MAJORITY_INDICATION]: string
  [MetadatumType.VALIDITY_RULE]: string
  [MetadatumType.LEAD_JURISDICTION]: string
  [MetadatumType.LEAD_UNIT]: string
  [MetadatumType.PARTICIPATION_TYPE]: string
  [MetadatumType.PARTICIPATION_INSTITUTION]: string
  [MetadatumType.SUBJECT_FNA]: string
  [MetadatumType.SUBJECT_PREVIOUS_FNA]: string
  [MetadatumType.SUBJECT_GESTA]: string
  [MetadatumType.SUBJECT_BGB_3]: string
}

export type Metadata = {
  [Type in MetadatumType]?: MetadataValueType[Type][] | undefined
}

export enum MetadataSectionName {
  NORM = "NORM",
  SUBJECT_AREA = "SUBJECT_AREA",
  LEAD = "LEAD",
  PARTICIPATION = "PARTICIPATION",
}

export type MetadataSections = {
  [Name in MetadataSectionName]?: (Metadata & MetadataSections)[] | undefined
}

export type FlatMetadata = {
  documentTemplateName?: string
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
  text?: string
}
