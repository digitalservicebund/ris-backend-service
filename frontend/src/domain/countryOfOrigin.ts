import EditableListItem from "./editableListItem"
import { FieldOfLaw } from "@/domain/fieldOfLaw"

export default class CountryOfOrigin implements EditableListItem {
  public id?: string // BE only
  public localId: string // FE only
  public legacyValue?: string
  public country?: FieldOfLaw
  public fieldOfLaw?: FieldOfLaw

  constructor(data: Partial<CountryOfOrigin> = {}) {
    Object.assign(this, data)
    this.localId = data.localId ?? crypto.randomUUID()
  }

  get isEmpty(): boolean {
    return !this.legacyValue && !this.country
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
