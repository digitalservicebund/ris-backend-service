import { Court } from "@/domain/court"
import { Decision } from "@/domain/decision"
import DocumentationOffice from "@/domain/documentationOffice"
import { DocumentType } from "@/domain/documentType"

export default class DocumentUnitListEntry {
  public id?: string
  uuid?: string
  documentNumber?: string
  decisionDate?: string
  createdAt?: string
  lastPublicationDateTime?: string
  scheduledPublicationDateTime?: string
  status?: NonNullable<Decision["status"]>
  fileNumber?: string
  documentType?: DocumentType
  court?: Court
  appraisalBody?: string
  hasHeadnoteOrPrinciple?: boolean
  hasAttachments?: boolean
  note?: string
  isDeletable?: boolean
  isEditable?: boolean
  source?: string
  creatingDocumentationOffice?: DocumentationOffice

  constructor(data: Partial<DocumentUnitListEntry> = {}) {
    Object.assign(this, data)
  }
}
