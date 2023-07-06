import dayjs from "dayjs"
import EditableListItem from "./editableListItem"
import LinkedDocumentUnit from "./linkedDocumentUnit"

export default class ProceedingDecision
  extends LinkedDocumentUnit
  implements EditableListItem
{
  public dateKnown = true

  static requiredFields = ["fileNumber", "court", "decisionDate"] as const

  constructor(data: Partial<ProceedingDecision> = {}) {
    super()
    Object.assign(this, data)
  }

  get renderDecision(): string {
    return [
      ...(this.court ? [`${this.court.label}`] : []),
      ...(this.documentType ? [this.documentType?.jurisShortcut] : []),
      ...(this.dateUnknown === true ? ["unbekanntes Entscheidungsdatum"] : []),
      ...(this.decisionDate
        ? [dayjs(this.decisionDate).format("DD.MM.YYYY")]
        : []),
      ...(this.fileNumber ? [this.fileNumber] : []),
      ...(this.documentNumber && this.isReadOnly ? [this.documentNumber] : []),
    ].join(", ")
  }

  get dateUnknown(): boolean {
    return !this.dateKnown
  }
  set dateUnknown(dateUnknown: boolean) {
    this.dateKnown = !dateUnknown
  }

  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  get missingRequiredFields() {
    return ProceedingDecision.requiredFields.filter((field) =>
      this.requiredFieldIsEmpty(field, this[field])
    )
  }

  private requiredFieldIsEmpty(
    fieldName: keyof ProceedingDecision,
    value: ProceedingDecision[(typeof ProceedingDecision.requiredFields)[number]]
  ) {
    if (fieldName === "decisionDate" && !value && !this.dateKnown) {
      return false
    }
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

export const proceedingDecisionFieldLabels: { [name: string]: string } = {
  court: "Gericht",
  decisionDate: "Entscheidungsdatum",
  fileNumber: "Aktenzeichen",
  documentType: "Dokumenttyp",
}
