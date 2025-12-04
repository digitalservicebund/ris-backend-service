import EditableListItem from "./editableListItem"
import { NormAbbreviation } from "./normAbbreviation"
import SingleNorm from "./singleNorm"

export default class NonApplicationNorm implements EditableListItem {
  public normAbbreviation?: NormAbbreviation
  public singleNorms?: SingleNorm[]

  static readonly requiredFields = ["normAbbreviation"] as const
  static readonly fields = ["normAbbreviation"] as const

  constructor(data: Partial<NonApplicationNorm> = {}) {
    Object.assign(this, data)
  }

  get id() {
    return this.normAbbreviation?.id
  }

  equals(entry: NonApplicationNorm): boolean {
    if (entry.isEmpty) return true
    let isEquals = false
    if (this.normAbbreviation) {
      isEquals = entry.normAbbreviation
        ? this.normAbbreviation?.abbreviation ==
          entry.normAbbreviation.abbreviation
        : false
    }
    return isEquals
  }

  get renderSummary(): string {
    return this.normAbbreviation?.abbreviation ?? ""
  }

  get isEmpty(): boolean {
    let isEmpty = true

    NonApplicationNorm.fields.map((key) => {
      if (!this.fieldIsEmpty(this[key])) {
        isEmpty = false
      }
    })
    return isEmpty
  }

  private fieldIsEmpty(
    value: NonApplicationNorm[(typeof NonApplicationNorm.fields)[number]],
  ) {
    return value === undefined || !value || Object.keys(value).length === 0
  }
}
