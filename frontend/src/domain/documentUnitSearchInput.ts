import DocumentationOffice from "./documentationOffice"
import DocumentUnit from "./documentUnit"

export default class DocumentUnitSearchInput {
  public id?: string
  uuid?: string
  documentNumberOrFileNumber?: string
  courtType?: string
  courtLocation?: string
  decisionDate?: string
  decisionDateEnd?: string
  status?: NonNullable<DocumentUnit["status"]>
  documentationOffice?: DocumentationOffice
  myDocOfficeOnly?: boolean = false

  constructor(data: Partial<DocumentUnitSearchInput> = {}) {
    Object.assign(this, data)
  }

  static fields = [
    "documentNumberOrFileNumber",
    "courtType",
    "courtLocation",
    "decisionDate",
    "decisionDateEnd",
    "status",
  ] as const
}
