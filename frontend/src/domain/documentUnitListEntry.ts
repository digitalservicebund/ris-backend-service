import { Court } from "@/domain/court"
import DocumentationOffice from "@/domain/documentationOffice"
import DocumentationUnitProcessStep from "@/domain/documentationUnitProcessStep"
import { DocumentType } from "@/domain/documentType"
import ProcessStep from "@/domain/processStep"
import { PublicationStatus } from "@/domain/publicationStatus"

export default class DocumentUnitListEntry {
  public id?: string
  uuid?: string
  documentNumber?: string
  decisionDate?: string
  resolutionDate?: string
  createdAt?: string
  lastPublicationDateTime?: string
  scheduledPublicationDateTime?: string
  status?: PublicationStatus
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
  currentDocumentationUnitProcessStep?: DocumentationUnitProcessStep
  previousProcessStep?: ProcessStep

  constructor(data: Partial<DocumentUnitListEntry> = {}) {
    Object.assign(this, data)
  }
}
