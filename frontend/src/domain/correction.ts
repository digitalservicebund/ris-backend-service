import EditableListItem from "./editableListItem"

export const CORRECTION_TYPES = [
  "Berichtigungsbeschluss",
  "Ergänzungsbeschluss",
  "Ergänzungsurteil",
  "Unrichtigkeiten",
  "Schreibfehlerberichtigung",
] as const

export type CorrectionType = (typeof CORRECTION_TYPES)[number]

export default class Correction implements EditableListItem {
  public id?: string // BE only
  public localId: string // FE only
  public newEntry?: boolean
  public type?: CorrectionType
  public description?: string
  public date?: string
  public borderNumbers?: number[]
  public content?: string

  static readonly fields = [
    "type",
    "description",
    "date",
    "borderNumbers",
    "content",
  ] as const

  constructor(data: Partial<Correction> = {}) {
    Object.assign(this, data)
    this.localId = data.localId ?? crypto.randomUUID()
  }
  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  get missingRequiredFields(): (typeof Correction.fields)[number][] {
    if (this.type == null) {
      return ["type"]
    }

    return []
  }

  get isEmpty(): boolean {
    return Correction.fields.every((field) => this.fieldIsEmpty(this[field]))
  }

  fieldIsEmpty(value: Correction[(typeof Correction.fields)[number]]) {
    return value === undefined || !value || Object.keys(value).length === 0
  }

  get renderSummary(): string {
    return "no render summary" // render is handled by a specific component
  }
}
