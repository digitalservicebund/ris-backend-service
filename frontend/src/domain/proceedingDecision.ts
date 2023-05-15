import dayjs from "dayjs"
import { Court, DocumentType } from "./documentUnit"

export class ProceedingDecision {
  public uuid?: string
  public documentNumber?: string
  public dataSource?: "NEURIS" | "MIGRATION" | "PROCEEDING_DECISION"
  public court?: Court
  public date?: string
  public fileNumber?: string
  public documentType?: DocumentType
  public dateKnown = true

  static requiredFields = ["fileNumber", "court", "date"] as const

  constructor(data: Partial<ProceedingDecision> = {}) {
    Object.assign(this, data)
  }

  get renderDecision(): string {
    return [
      ...(this.court ? [`${this.court.label}`] : []),
      ...(this.documentType ? [this.documentType?.jurisShortcut] : []),
      ...(this.date ? [dayjs(this.date).format("DD.MM.YYYY")] : []),
      ...(this.dateUnknown ? ["unbekanntes Entscheidungsdatum"] : []),
      ...(this.fileNumber ? [this.fileNumber] : []),
      ...(this.documentNumber && this.hasLink ? [this.documentNumber] : []),
    ].join(", ")
  }

  get hasLink(): boolean {
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
