import { Court } from "@/domain/court"
import DocumentationOffice from "@/domain/documentationOffice"
import { DocumentType } from "@/domain/documentType"
import Reference from "@/domain/reference"
import { Source } from "@/domain/source"

export type DocumentationUnitCreationParameters = {
  documentationOffice?: DocumentationOffice
  documentType?: DocumentType
  decisionDate?: string
  fileNumber?: string
  court?: Court
  reference?: Reference
  sources?: Source[]
}
