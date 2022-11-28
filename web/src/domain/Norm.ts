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

export interface Norm {
  readonly guid: string
  readonly articles: Article[]
  longTitle: string
  officialShortTitle?: string
  officialAbbreviation?: string
  referenceNumber?: string
  publicationDate?: string
  announcementDate?: string
  citationDate?: string
  frameKeywords?: string
  authorEntity?: string
  authorDecidingBody?: string
  authorIsResolutionMajority?: boolean
  leadJurisdiction?: string
  leadUnit?: string
  participationType?: string
  participationInstitution?: string
  documentTypeName?: string
  documentNormCategory?: string
  documentTemplateName?: string
  subjectFna?: string
  subjectPreviousFna?: string
  subjectGesta?: string
  subjectBgb3?: string
  unofficialTitle: string
  unofficialShortTitle: string
  unofficialAbbreviation: string
  risAbbreviation: string
}
