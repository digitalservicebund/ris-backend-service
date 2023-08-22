import DocumentationOffice from "./documentationOffice"
import DocumentUnit, { Court } from "./documentUnit"

export default class DocumentUnitSearchInput {
  public id?: string
  uuid?: string
  documentNumberOrFileNumber?: string
  court?: Court
  decisionDate?: string
  decisionDateEnd?: string
  status?: NonNullable<DocumentUnit["status"]>
  documentationOffice?: DocumentationOffice
  myDocOfficeOnly?: boolean = false

  constructor(data: Partial<DocumentUnitSearchInput> = {}) {
    Object.assign(this, data)
  }
}
