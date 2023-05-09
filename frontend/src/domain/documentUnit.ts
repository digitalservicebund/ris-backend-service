import dayjs from "dayjs"

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
  center?: string
  region?: string
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

export class ProceedingDecision {
  public uuid?: string
  public documentNumber?: string
  public dataSource?: "NEURIS" | "MIGRATION" | "PROCEEDING_DECISION"
  public court?: Court
  public date?: string
  public fileNumber?: string
  public documentType?: DocumentType

  static requiredFields = ["fileNumber", "court", "date"] as const

  constructor(data: Partial<ProceedingDecision> = {}) {
    Object.assign(this, data)
  }

  get renderDecision(): string {
    return [
      ...(this.court ? [`${this.court.label}`] : []),
      ...(this.documentType ? [this.documentType?.jurisShortcut] : []),
      ...(this.date ? [dayjs(this.date).format("DD.MM.YYYY")] : []),
      ...(this.fileNumber ? [this.fileNumber] : []),
      ...(this.documentNumber && this.hasLink ? [this.documentNumber] : []),
    ].join(", ")
  }

  get hasLink(): boolean {
    return this.dataSource !== "PROCEEDING_DECISION"
  }

  get missingRequiredFields() {
    return ProceedingDecision.requiredFields.filter((field) =>
      this.requiredFieldIsEmpty(this[field] as keyof ProceedingDecision)
    )
  }

  private requiredFieldIsEmpty(
    value: ProceedingDecision[(typeof ProceedingDecision.requiredFields)[number]]
  ) {
    if (value === undefined || !value || value === null) {
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
