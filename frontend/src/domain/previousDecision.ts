import dayjs from "dayjs"
import EditableListItem from "./editableListItem"
import RelatedDocumentation from "./relatedDocumentation"

export default class PreviousDecision
  extends RelatedDocumentation
  implements EditableListItem
{
  public dateKnown = true

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
      ...(this.court ? [`${this.court.label}`] : []),
      ...(this.decisionDate
        ? [dayjs(this.decisionDate).format("DD.MM.YYYY")]
        : []),
      ...(this.dateUnknown === true ? ["unbekanntes Entscheidungsdatum"] : []),
      ...(this.fileNumber ? [this.fileNumber] : []),
      ...(this.documentType ? [this.documentType?.jurisShortcut] : []),
      // ...(this.documentNumber && this.hasForeignSource
      //   ? [this.documentNumber]
      //   : []),
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
    // Todo implement linked logic
    // return this.hasForeignSource
    return false
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

export const previousDecisionFieldLabels: { [name: string]: string } = {
  court: "Gericht",
  decisionDate: "Entscheidungsdatum",
  fileNumber: "Aktenzeichen",
  documentType: "Dokumenttyp",
}
