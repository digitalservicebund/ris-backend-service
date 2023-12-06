import dayjs from "dayjs"
import EditableListItem from "./editableListItem"
import RelatedDocumentation from "./relatedDocumentation"

export default class EnsuingDecision
  extends RelatedDocumentation
  implements EditableListItem
{
  public pending: boolean | undefined
  public note: string | undefined

  static requiredFields = ["fileNumber", "court", "decisionDate"] as const
  static fields = [
    "pending",
    "fileNumber",
    "court",
    "decisionDate",
    "documentType",
    "note",
  ] as const

  constructor(data: Partial<EnsuingDecision> = {}) {
    super()
    Object.assign(this, data)
  }

  get renderDecision(): string {
    return [
      ...(this.pending === true ? ["anhängig"] : ["nachgehend"]),
      ...(this.court ? [`${this.court?.label}`] : []),
      ...(this.decisionDate
        ? [dayjs(this.decisionDate).format("DD.MM.YYYY")]
        : []),
      ...(this.pending ? ["Datum unbekannt"] : []),
      ...(this.fileNumber ? [this.fileNumber] : []),
      ...(this.documentType ? [this.documentType?.jurisShortcut] : []),
      ...(this.note ? [this.note] : []),
      ...(this.documentNumber ? [this.documentNumber] : []),
    ].join(", ")
  }

  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  get missingRequiredFields() {
    return EnsuingDecision.requiredFields.filter((field) =>
      this.fieldIsEmpty(field, this[field]),
    )
  }

  get isReadOnly(): boolean {
    return this.documentNumber != null
  }

  get isEmpty(): boolean {
    let isEmpty = true

    EnsuingDecision.fields.map((field) => {
      if (!this.fieldIsEmpty(field, this[field])) {
        isEmpty = false
      }
    })
    return isEmpty
  }

  private fieldIsEmpty(
    fieldName: keyof EnsuingDecision,
    value: EnsuingDecision[(typeof EnsuingDecision.fields)[number]],
  ) {
    if (fieldName === "pending" && value === true) {
      return false
    }

    if (fieldName === "decisionDate" && this.pending === true) {
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

export const ensuingDecisionFieldLabels: { [name: string]: string } = {
  court: "Gericht",
  decisionDate: "Entscheidungsdatum",
  fileNumber: "Aktenzeichen",
  documentType: "Dokumenttyp",
}
