import { ProceedingDecision } from "./proceedingDecision"
import { NormReference } from "@/domain/normReference"

export type CoreData = {
  fileNumbers?: string[]
  deviatingFileNumbers?: string[]
  court?: Court
  incorrectCourts?: string[]
  documentType?: DocumentType
  procedure?: string
  ecli?: string
  deviatingEclis?: string[]
  appraisalBody?: string
  decisionDate?: string
  deviatingDecisionDates?: string[]
  legalEffect?: string
  inputType?: string
  documentationOffice?: DocumentationOffice
  region?: string
}

export type ContentRelatedIndexing = {
  norms?: NormReference[]
}

export type DocumentType = {
  jurisShortcut: string
  label: string
}

export type Court = {
  type: string
  location: string
  label: string
  revoked?: string
}

export type Texts = {
  decisionName?: string
  headline?: string
  guidingPrinciple?: string
  headnote?: string
  tenor?: string
  reasons?: string
  caseFacts?: string
  decisionReasons?: string
}

export type DocumentationOffice = {
  label: string
  abbreviation?: string
}

export default class DocumentUnit {
  readonly uuid: string
  readonly id?: string
  readonly documentNumber?: string
  readonly creationtimestamp?: string

  public fileuploadtimestamp?: string
  public s3path?: string
  public filetype?: string
  public filename?: string

  public coreData: CoreData = {}
  public texts: Texts = {}
  public proceedingDecisions?: ProceedingDecision[]
  public contentRelatedIndexing: ContentRelatedIndexing = {}

  static requiredFields = [
    "fileNumbers",
    "court",
    "decisionDate",
    "legalEffect",
    "documentType",
  ] as const

  constructor(uuid: string, data: Partial<DocumentUnit> = {}) {
    this.uuid = String(uuid)

    let rootField: keyof DocumentUnit
    for (rootField in data) {
      if (data[rootField] === null) delete data[rootField]
    }
    let coreDataField: keyof CoreData
    for (coreDataField in data.coreData) {
      if (data.coreData && data.coreData[coreDataField] === null)
        delete data.coreData[coreDataField]
    }
    let textsField: keyof Texts
    for (textsField in data.texts) {
      if (data.texts && data.texts[textsField] === null)
        delete data.texts[textsField]
    }

    if (data.proceedingDecisions)
      data.proceedingDecisions = data.proceedingDecisions.map(
        (decision) => new ProceedingDecision({ ...decision })
      )

    Object.assign(this, data)
  }
  get hasFile(): boolean {
    return !!this.s3path
  }
  get missingRequiredFields() {
    return DocumentUnit.requiredFields.filter((field) =>
      this.isEmpty(this.coreData[field])
    )
  }
  public static isRequiredField(fieldName: string) {
    return DocumentUnit.requiredFields.some(
      (requiredfieldName) => requiredfieldName === fieldName
    )
  }
  public isEmpty(
    value: CoreData[(typeof DocumentUnit.requiredFields)[number]]
  ) {
    if (value === undefined || !value) {
      return true
    }
    if (value instanceof Array && value.length === 0) {
      return true
    }
    if (typeof value === "object" && "location" in value && "type" in value) {
      return value.location === "" && value.type === ""
    }
    return false
  }
}
