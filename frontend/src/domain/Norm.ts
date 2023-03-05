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

export type FrameData = {
  documentTemplateName?: string
  leadUnit?: string
  participationInstitution?: string
  subjectBgb3?: string
  ageIndicationEnd?: string
  ageIndicationStart?: string
  ageOfMajorityIndication?: string
  announcementDate?: string
  applicationScopeArea?: string
  applicationScopeEndDate?: string
  applicationScopeStartDate?: string
  categorizedReference?: string
  celexNumber?: string
  citationDate?: string
  completeCitation?: string
  definition?: string
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
  divergentDocumentNumber?: string
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
  frameKeywords?: string
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
  referenceNumber?: string
  reissueArticle?: string
  reissueDate?: string
  reissueNote?: string
  reissueReference?: string
  repealArticle?: string
  repealDate?: string
  repealNote?: string
  repealReferences?: string
  risAbbreviation?: string
  risAbbreviationInternationalLaw?: string
  statusDate?: string
  statusDescription?: string
  statusNote?: string
  statusReference?: string
  subjectFna?: string
  subjectGesta?: string
  subjectPreviousFna?: string
  text?: string
  unofficialAbbreviation?: string
  unofficialLongTitle?: string
  unofficialReference?: string
  unofficialShortTitle?: string
  validityRule?: string
}

export interface Norm extends FrameData {
  readonly guid: string
  readonly articles: Article[]
  readonly files?: FileReference[]
}

export interface FileReference {
  readonly name: string
  readonly hash: string
  readonly createdAt: string
}
