import DocumentationOffice from "./documentationOffice"
import DocumentUnit, { Court } from "./documentUnit"
import { DocumentType } from "@/domain/documentUnit"

export default class DocumentUnitListEntry {
  public id?: string
  uuid?: string
  documentNumber?: string
  decisionDate?: string
  status?: NonNullable<DocumentUnit["status"]>
  fileName?: string
  fileNumber?: string
  documentationOffice?: DocumentationOffice
  documentType?: DocumentType
  court?: Court
  myDocOfficeOnly: boolean = false

  constructor(data: Partial<DocumentUnitListEntry> = {}) {
    Object.assign(this, data)
  }
}
