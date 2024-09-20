import EditableListItem from "./editableListItem"
import IcBaselinePerson from "~icons/ic/baseline-person"

export default class ParticipatingJudge implements EditableListItem {
  public id?: string
  public name?: string
  public referencedOpinions?: string

  static readonly requiredFields = ["name"] as const

  static readonly fields = ["name", "referencedOpinions"] as const

  constructor(data: Partial<ParticipatingJudge> = {}) {
    Object.assign(this, data)
    if (this.id == undefined) {
      this.id = crypto.randomUUID()
    }
  }

  get renderDecision(): string {
    return [
      ...(this.name ? [this.name] : []),
      ...(this.referencedOpinions ? [`(${this.referencedOpinions})`] : []),
    ].join(" ")
  }

  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  get missingRequiredFields() {
    return ParticipatingJudge.requiredFields.filter((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }

  get isEmpty(): boolean {
    return ParticipatingJudge.fields.every((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }

  equals(entry: ParticipatingJudge): boolean {
    return this.id === entry.id
  }

  get nameIsSet(): boolean {
    return !!this.name
  }

  get getIcon() {
    return IcBaselinePerson
  }

  get hasForeignSource() {
    return false
  }

  private fieldIsEmpty(
    value: ParticipatingJudge[(typeof ParticipatingJudge.fields)[number]],
  ): boolean {
    return !value
  }
}
