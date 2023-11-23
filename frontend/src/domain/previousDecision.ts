import dayjs from "dayjs"
import EditableListItem from "./editableListItem"
import RelatedDocumentation from "./relatedDocumentation"

export default class PreviousDecision
  extends RelatedDocumentation
  implements EditableListItem
{
  public dateKnown: boolean = true

  static requiredFields = ["fileNumber", "court", "decisionDate"] as const
  static fields = [
    "fileNumber",
    "court",
    "decisionDate",
    "documentType",
  ] as const

  constructor(data: Partial<PreviousDecision> = {}) {
    super()
    Object.assign(this, data)
  }

  get renderDecision(): string {
    return [
      ...(this.court ? [`${this.court?.label}`] : []),
      ...(this.decisionDate
        ? [dayjs(this.decisionDate).format("DD.MM.YYYY")]
        : []),
      ...(this.dateKnown === false ? ["Datum unbekannt"] : []),
      ...(this.fileNumber ? [this.fileNumber] : []),
      ...(this.documentType ? [this.documentType?.jurisShortcut] : []),
      ...(this.documentNumber && this.hasForeignSource
        ? [this.documentNumber]
        : []),
    ].join(", ")
  }

  get dateUnknown(): boolean {
    return this.dateKnown === false
  }
  set dateUnknown(dateUnknown: boolean) {
    this.dateKnown = !dateUnknown
  }

  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  get isReadOnly(): boolean {
    return this.hasForeignSource
  }

  get missingRequiredFields() {
    return PreviousDecision.requiredFields.filter((field) =>
      this.fieldIsEmpty(field, this[field]),
    )
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

  private fieldIsEmpty(
    fieldName: keyof PreviousDecision,
    value: PreviousDecision[(typeof PreviousDecision.fields)[number]],
  ) {
    if (fieldName === "decisionDate" && this.dateKnown === false && !value) {
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

export const previousDecisionFieldLabels: { [name: string]: string } = {
  court: "Gericht",
  decisionDate: "Entscheidungsdatum",
  fileNumber: "Aktenzeichen",
  documentType: "Dokumenttyp",
}
