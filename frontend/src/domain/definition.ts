import EditableListItem from "./editableListItem"

export default class Definition implements EditableListItem {
  public id?: string
  public newEntry?: boolean
  public definedTerm?: string
  public definingBorderNumber?: number

  static readonly requiredFields = ["definedTerm"] as const

  static readonly fields = ["definedTerm", "definingBorderNumber"] as const

  constructor(data: Partial<Definition> = {}) {
    Object.assign(this, data)

    if (this.id == undefined) {
      this.id = crypto.randomUUID()
      this.newEntry = true
    } else if (data.newEntry == undefined) {
      this.newEntry = false
    }
  }

  get renderSummary(): string {
    return [
      ...(this.definedTerm ? [this.definedTerm] : []),
      ...(this.definingBorderNumber ? [this.definingBorderNumber] : []),
    ].join(" | ")
  }

  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  get missingRequiredFields() {
    return Definition.requiredFields.filter((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }

  get isEmpty(): boolean {
    return Definition.fields.every((field) => this.fieldIsEmpty(this[field]))
  }

  equals(entry: Definition): boolean {
    return this.id === entry.id
  }

  private fieldIsEmpty(
    value: Definition[(typeof Definition.fields)[number]],
  ): boolean {
    return !value
  }
}
