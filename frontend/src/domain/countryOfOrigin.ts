import EditableListItem from "./editableListItem"
import { FieldOfLaw } from "@/domain/fieldOfLaw"

export default class CountryOfOrigin implements EditableListItem {
  public id?: string
  public newEntry?: boolean
  public legacyValue?: string
  public country?: FieldOfLaw
  public fieldOfLaw?: FieldOfLaw

  constructor(data: Partial<CountryOfOrigin> = {}) {
    Object.assign(this, data)

    if (this.id == undefined) {
      this.id = crypto.randomUUID()
      this.newEntry = true
    } else if (data.newEntry == undefined) {
      this.newEntry = false
    }
  }

  get isEmpty(): boolean {
    return !this.legacyValue && !this.country
  }

  equals(entry: CountryOfOrigin): boolean {
    return this.id === entry.id
  }

  get renderSummary(): string {
    let summary = ""

    if (this.legacyValue) {
      summary += `${this.legacyValue} `
    }

    summary += [this.country, this.fieldOfLaw]
      .filter((fieldOfLaw) => fieldOfLaw != undefined)
      .map(
        (fieldOfLaw: FieldOfLaw) =>
          `${fieldOfLaw.identifier} ${fieldOfLaw.text}`,
      )
      .join(", ")

    return summary
  }
}
