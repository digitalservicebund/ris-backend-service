import EditableListItem from "./editableListItem"

export type LanguageCode = {
  id: string
  label: string
}

export default class ForeignLanguageVersion implements EditableListItem {
  public id?: string // BE only
  public localId: string // FE only
  public newEntry?: boolean
  public languageCode?: LanguageCode
  public link?: string

  static readonly fields = ["languageCode", "link"] as const

  constructor(data: Partial<ForeignLanguageVersion> = {}) {
    Object.assign(this, data)
    this.localId = data.localId ?? crypto.randomUUID()
  }

  get isEmpty(): boolean {
    return ForeignLanguageVersion.fields.some((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }

  equals(entry: ForeignLanguageVersion): boolean {
    return this.localId === entry.localId
  }

  private fieldIsEmpty(
    value: ForeignLanguageVersion[(typeof ForeignLanguageVersion.fields)[number]],
  ): boolean {
    return !value
  }
}
