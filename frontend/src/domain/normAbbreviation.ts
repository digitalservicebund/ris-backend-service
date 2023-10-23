export type NormAbbreviation = {
  id?: string
  abbreviation: string
  decisionDate?: string
  documentId?: string
  documentNumber?: string
  officialLetterAbbreviation?: string
  officialLongTitle?: string
  officialShortTitle?: string
  source?: string
  documentTypes?: string[]
  regions?: Region[]
}

export type Region = {
  code: string
}
