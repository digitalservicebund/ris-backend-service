import dayjs from "dayjs"
import LinkedDocumentUnit from "./linkedDocumentUnit"

export default class ProceedingDecision extends LinkedDocumentUnit {
  public dataSource?: "NEURIS" | "MIGRATION" | "PROCEEDING_DECISION"
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
      ...(this.documentNumber && this.isDocUnit() ? [this.documentNumber] : []),
    ].join(", ")
  }

  public isDocUnit(): boolean {
    return this.dataSource !== "PROCEEDING_DECISION"
  }

  get dateUnknown(): boolean {
    return !this.dateKnown
  }
  set dateUnknown(dateUnknown: boolean) {
    this.dateKnown = !dateUnknown
  }

  get missingRequiredFields() {
    return ProceedingDecision.requiredFields.filter((field) =>
      this.requiredFieldIsEmpty(field, this[field] as keyof ProceedingDecision)
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
