import DocumentationOffice from "./documentationOffice"
import Reference from "./reference"
import { Court } from "@/domain/court"
import { DocumentType } from "@/domain/documentType"

export type DocumentationUnitParameters = {
  documentationOffice?: DocumentationOffice
  documentType?: DocumentType
  decisionDate?: string
  fileNumber?: string
  court?: Court
  reference?: Reference
}

export type DocumentUnitSearchParameter =
  | "documentNumber"
  | "fileNumber"
  | "publicationStatus"
  | "publicationDate"
  | "scheduledOnly"
  | "courtType"
  | "courtLocation"
  | "decisionDate"
  | "decisionDateEnd"
  | "withError"
  | "myDocOfficeOnly"
  | "withDuplicateWarning"

export type EurlexParameters = {
  documentationOffice: DocumentationOffice
  celexNumbers: string[]
}

export enum Kind {
  DOCUMENTION_UNIT = "DOCUMENTION_UNIT",
  PENDING_PROCEEDING = "PENDING_PROCEEDING",
}
