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
}
