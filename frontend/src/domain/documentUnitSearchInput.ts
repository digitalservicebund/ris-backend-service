export default class DocumentUnitSearchInput {
  public id?: string
  uuid?: string
  documentNumberOrFileNumber?: string
  courtType?: string
  courtLocation?: string
  decisionDate?: string
  decisionDateEnd?: string
  publicationStatus?: string
  withError?: string
  myDocOfficeOnly?: string

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
