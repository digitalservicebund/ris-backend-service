import dayjs from "dayjs"
import LinkedDocumentUnit from "./linkedDocumentUnit"

export default class ActiveCitation extends LinkedDocumentUnit {
  public predicateList?: string

  static requiredFields = ["court"] as const

  constructor(data: Partial<ActiveCitation> = {}) {
    super()
    Object.assign(this, data)
  }

  get renderDecision(): string {
    return [
      ...(this.court?.label ? [`${this.court?.label}`] : []),
      ...(this.decisionDate
        ? [dayjs(this.decisionDate).format("DD.MM.YYYY")]
        : []),
      ...(this.documentType ? [this.documentType] : []),
      ...(this.fileNumber ? [this.fileNumber] : []),
      ...(this.predicateList ? [this.predicateList] : []),
    ].join(", ")
  }

  get missingRequiredFields() {
    return ActiveCitation.requiredFields.filter((field) =>
      this.requiredFieldIsEmpty(this[field])
    )
  }

  private requiredFieldIsEmpty(
    value: ActiveCitation[(typeof ActiveCitation.requiredFields)[number]]
  ) {
    if (value === undefined || !value || value === null) {
      return true
    }

    return false
  }
}
