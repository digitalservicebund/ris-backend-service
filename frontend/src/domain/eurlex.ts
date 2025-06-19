import DocumentationOffice from "@/domain/documentationOffice"

export default interface EURLexResult {
  ecli: string
  celex: string
  courtType: string
  courtLocation: string
  date: string
  title: string
  fileNumber: string
  publicationDate: string
  uri: string
  htmlLink?: string
}
export type EurlexParameters = {
  documentationOffice: DocumentationOffice
  celexNumbers: string[]
}
