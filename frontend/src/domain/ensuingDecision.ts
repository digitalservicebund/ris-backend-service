import dayjs from "dayjs"
import EditableListItem from "./editableListItem"
import RelatedDocumentation from "./relatedDocumentation"

export default class EnsuingDecision
  extends RelatedDocumentation
  implements EditableListItem
{
  public pending?: boolean
  public note?: string

  static readonly requiredFields = [
    "fileNumber",
    "court",
    "decisionDate",
  ] as const
  static readonly fields = [
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

  equals(entry: EnsuingDecision): boolean {
    return this.localId === entry.localId
  }

  get renderSummary(): string {
    return [
      ...(this.pending === true ? ["anhängig"] : ["nachgehend"]),
      ...(this.court ? [`${this.court?.label}`] : []),
      ...(this.decisionDate
        ? [dayjs(this.decisionDate).format("DD.MM.YYYY")]
        : []),
      ...(this.pending ? ["Datum unbekannt"] : []),
      ...(this.fileNumber ? [this.fileNumber] : []),
      ...(this.documentType ? [this.documentType?.label] : []),
      ...(this.note ? [this.note] : []),
    ].join(", ")
  }

  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  get missingRequiredFields() {
    return EnsuingDecision.requiredFields.filter((field) => {
      if (field === "decisionDate" && this.pending === true) {
        return false
      } else return this.fieldIsEmpty(field, this[field])
    })
  }

  get isReadOnly(): boolean {
    return false
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

  get showSummaryOnEdit(): boolean {
    return this.hasForeignSource
  }

  private fieldIsEmpty(
    fieldName: keyof EnsuingDecision,
    value: EnsuingDecision[(typeof EnsuingDecision.fields)[number]],
  ) {
    if (value === undefined || !value) {
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

export const ensuingDecisionFieldLabels: { [name: string]: string } = {
  court: "Gericht",
  decisionDate: "Entscheidungsdatum",
  fileNumber: "Aktenzeichen",
  documentType: "Dokumenttyp",
}
