import dayjs from "dayjs"
import { CitationStyle } from "./citationStyle"
import LinkedDocumentUnit from "./linkedDocumentUnit"

export default class ActiveCitation extends LinkedDocumentUnit {
  public citationStyle?: CitationStyle

  static requiredFields = [
    "citationStyle",
    "fileNumber",
    "court",
    "decisionDate",
  ] as const

  constructor(data: Partial<ActiveCitation> = {}) {
    super()
    Object.assign(this, data)
  }

  get renderDecision(): string {
    return [
      ...(this.citationStyle?.label ? [this.citationStyle.label] : []),
      ...(this.court?.label ? [`${this.court?.label}`] : []),
      ...(this.decisionDate
        ? [dayjs(this.decisionDate).format("DD.MM.YYYY")]
        : []),
      ...(this.fileNumber ? [this.fileNumber] : []),
      ...(this.documentType ? [this.documentType.label] : []),
      ...(this.documentNumber && this.isDocUnit() ? [this.documentNumber] : []),
    ].join(", ")
  }

  get missingRequiredFields() {
    return ActiveCitation.requiredFields.filter((field) =>
      this.requiredFieldIsEmpty(this[field])
    )
  }

  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
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
