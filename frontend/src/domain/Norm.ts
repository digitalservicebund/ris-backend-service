export interface Norm extends NormBase, FlatMetadata {
  metadataSections?: MetadataSections
}

export interface NormBase {
  readonly guid: string
  articles: Article[]
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
  YEAR = "YEAR",
  DATE = "DATE",
  RANGE_START = "RANGE_START",
  RANGE_END = "RANGE_END",
  ANNOUNCEMENT_MEDIUM = "ANNOUNCEMENT_MEDIUM",
  ANNOUNCEMENT_GAZETTE = "ANNOUNCEMENT_GAZETTE",
  ADDITIONAL_INFO = "ADDITIONAL_INFO",
  EXPLANATION = "EXPLANATION",
  AREA_OF_PUBLICATION = "AREA_OF_PUBLICATION",
  NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA = "NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA",
  EU_GOVERNMENT_GAZETTE = "EU_GOVERNMENT_GAZETTE",
  SERIES = "SERIES",
  OTHER_OFFICIAL_REFERENCE = "OTHER_OFFICIAL_REFERENCE",
  NUMBER = "NUMBER",
  PAGE = "PAGE",
  EDITION = "EDITION",
  ENTITY = "ENTITY",
  DECIDING_BODY = "DECIDING_BODY",
  RESOLUTION_MAJORITY = "RESOLUTION_MAJORITY",
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
  [MetadatumType.DATE]: string
  [MetadatumType.YEAR]: string
  [MetadatumType.RANGE_START]: string
  [MetadatumType.RANGE_END]: string
  [MetadatumType.ANNOUNCEMENT_MEDIUM]: string
  [MetadatumType.ANNOUNCEMENT_GAZETTE]: string
  [MetadatumType.ADDITIONAL_INFO]: string
  [MetadatumType.EXPLANATION]: string
  [MetadatumType.AREA_OF_PUBLICATION]: string
  [MetadatumType.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA]: string
  [MetadatumType.EU_GOVERNMENT_GAZETTE]: string
  [MetadatumType.SERIES]: string
  [MetadatumType.OTHER_OFFICIAL_REFERENCE]: string
  [MetadatumType.NUMBER]: string
  [MetadatumType.PAGE]: string
  [MetadatumType.EDITION]: string
  [MetadatumType.ENTITY]: string
  [MetadatumType.DECIDING_BODY]: string
  [MetadatumType.RESOLUTION_MAJORITY]: boolean
  [MetadatumType.ADDITIONAL_INFO]: string
  [MetadatumType.EXPLANATION]: string
  [MetadatumType.ADDITIONAL_INFO]: string
  [MetadatumType.EXPLANATION]: string
  [MetadatumType.ANNOUNCEMENT_MEDIUM]: string
  [MetadatumType.AREA_OF_PUBLICATION]: string
  [MetadatumType.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA]: string
  [MetadatumType.SERIES]: string
  [MetadatumType.OTHER_OFFICIAL_REFERENCE]: string
  [MetadatumType.EU_GOVERNMENT_GAZETTE]: string
}

export type Metadata = {
  [Type in MetadatumType]?: MetadataValueType[Type][] | undefined
}

export enum MetadataSectionName {
  NORM = "NORM",
  SUBJECT_AREA = "SUBJECT_AREA",
  LEAD = "LEAD",
  PARTICIPATION = "PARTICIPATION",
  CITATION_DATE = "CITATION_DATE",
  AGE_INDICATION = "AGE_INDICATION",
  DIGITAL_ANNOUNCEMENT = "DIGITAL_ANNOUNCEMENT",
  PRINT_ANNOUNCEMENT = "PRINT_ANNOUNCEMENT",
  EU_ANNOUNCEMENT = "EU_ANNOUNCEMENT",
  OTHER_OFFICIAL_ANNOUNCEMENT = "OTHER_OFFICIAL_ANNOUNCEMENT",
  NORM_PROVIDER = "NORM_PROVIDER",
  OFFICIAL_REFERENCE = "OFFICIAL_REFERENCE",
}

export type MetadataSections = {
  [Name in MetadataSectionName]?: (Metadata & MetadataSections)[] | undefined
}

export type FlatMetadata = {
  documentTemplateName?: string
  announcementDate?: string
  applicationScopeArea?: string
  applicationScopeEndDate?: string
  applicationScopeStartDate?: string
  categorizedReference?: string
  celexNumber?: string
  completeCitation?: string
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
  otherStatusNote?: string
  principleEntryIntoForceDate?: string
  principleEntryIntoForceDateState?: string
  principleExpirationDate?: string
  principleExpirationDateState?: string
  printAnnouncementGazette?: string
  printAnnouncementPage?: string
  printAnnouncementYear?: string
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
