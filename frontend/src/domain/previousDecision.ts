import dayjs from "dayjs"
import EditableListItem from "./editableListItem"
import RelatedDocumentation from "./relatedDocumentation"

export default class PreviousDecision
  extends RelatedDocumentation
  implements EditableListItem
{
  public dateKnown: boolean = true

  static readonly requiredFields = [
    "fileNumber",
    "court",
    "decisionDate",
  ] as const
  static readonly fields = [
    "fileNumber",
    "deviatingFileNumber",
    "court",
    "decisionDate",
    "documentType",
  ] as const

  constructor(data: Partial<PreviousDecision> = {}) {
    super()
    Object.assign(this, data)
    if (this.uuid == undefined) {
      this.uuid = crypto.randomUUID()
      this.newEntry = true
    }
  }

  get id() {
    return this.uuid
  }

  get renderSummary(): string {
    return [
      ...(this.court ? [`${this.court?.label}`] : []),
      ...(this.decisionDate
        ? [dayjs(this.decisionDate).format("DD.MM.YYYY")]
        : []),
      ...(!this.dateKnown ? ["Datum unbekannt"] : []),
      ...(this.fileNumber ? [this.fileNumber] : []),
      ...(this.deviatingFileNumber ? [this.deviatingFileNumber] : []),
      ...(this.documentType ? [this.documentType?.label] : []),
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

  get isReadOnly(): boolean {
    return false
  }

  get missingRequiredFields() {
    return PreviousDecision.requiredFields.filter((field) => {
      if (field === "decisionDate" && this.dateKnown === false) {
        return false
      } else return this.fieldIsEmpty(field, this[field])
    })
  }

  get isEmpty(): boolean {
    let isEmpty = true

    PreviousDecision.fields.map((field) => {
      if (!this.fieldIsEmpty(field, this[field])) {
        isEmpty = false
      }
    })
    return isEmpty
  }

  equals(entry: PreviousDecision): boolean {
    return this.id === entry.id
  }

  get showSummaryOnEdit(): boolean {
    return true
  }

  private fieldIsEmpty(
    fieldName: keyof PreviousDecision,
    value: PreviousDecision[(typeof PreviousDecision.fields)[number]],
  ) {
    if (!value) {
      return true
    }
    if (value instanceof Array && value.length === 0) {
      return true
    }
    if (
      typeof value === "object" &&
      fieldName === "court" &&
      "location" in value &&
      "type" in value
    ) {
      return value.location === "" && value.type === ""
    }
    return false
  }
}

export const previousDecisionFieldLabels: Record<
  (typeof PreviousDecision.fields)[number],
  string
> = {
  court: "Gericht",
  decisionDate: "Entscheidungsdatum",
  fileNumber: "Aktenzeichen",
  deviatingFileNumber: "Abweichendes Aktenzeichen Vorinstanz",
  documentType: "Dokumenttyp",
}
