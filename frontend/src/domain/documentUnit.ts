export type CoreData = {
  fileNumbers?: string[]
  deviatingFileNumbers?: string[]
  court?: Court
  incorrectCourts?: string[]
  category?: string // TODO should this be DocumentType from lookupTables.ts?
  procedure?: string
  ecli?: string
  appraisalBody?: string
  decisionDate?: string
  legalEffect?: string
  inputType?: string
  center?: string
  region?: string
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

export type PreviousDecision = {
  courtType?: string
  courtPlace?: string
  date?: string
  fileNumber?: string
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
  public previousDecisions?: PreviousDecision[]

  static requiredFields = [
    "fileNumbers",
    "court",
    "decisionDate",
    "legalEffect",
    "category",
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
  public isEmpty(value: CoreData[typeof DocumentUnit.requiredFields[number]]) {
    if (value === undefined || !value) {
      return true
    }
    if (value instanceof Array && value.length === 0) {
      return true
    }
    return false
  }
}
