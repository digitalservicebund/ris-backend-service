import EditableListItem from "./editableListItem"

export type LanguageCode = {
  id: string
  label: string
}

export default class ForeignLanguageVersion implements EditableListItem {
  public id?: string
  public newEntry?: boolean
  public languageCode?: LanguageCode
  public link?: string

  static readonly fields = ["languageCode", "link"] as const

  constructor(data: Partial<ForeignLanguageVersion> = {}) {
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
      ...(this.languageCode?.label ? [this.languageCode.label] : []),
      ...(this.link ? [this.link] : []),
    ].join(": ")
  }

  get isEmpty(): boolean {
    return ForeignLanguageVersion.fields.some((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }

  equals(entry: ForeignLanguageVersion): boolean {
    return this.id === entry.id
  }

  private fieldIsEmpty(
    value: ForeignLanguageVersion[(typeof ForeignLanguageVersion.fields)[number]],
  ): boolean {
    return !value
  }
}
