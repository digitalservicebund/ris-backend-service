import dayjs from "dayjs"
import { Court, DocumentType } from "@/domain/documentUnit"

export default class ActiveCitation {
  public court?: Court
  public decisionDate?: string
  public documentType?: DocumentType
  public fileNumber?: string
  public predicateList?: string

  static requiredFields = ["court"] as const

  constructor(data: Partial<ActiveCitation> = {}) {
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
