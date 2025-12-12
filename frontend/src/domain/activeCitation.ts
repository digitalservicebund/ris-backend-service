import dayjs from "dayjs"
import { CitationType } from "./citationType"
import EditableListItem from "./editableListItem"
import RelatedDocumentation from "./relatedDocumentation"

export default class ActiveCitation
  extends RelatedDocumentation
  implements EditableListItem
{
  public citationType?: CitationType

  static readonly requiredFields = [
    "citationType",
    "fileNumber",
    "court",
    "decisionDate",
  ] as const

  static readonly fields = [
    "citationType",
    "fileNumber",
    "court",
    "decisionDate",
    "documentType",
  ] as const

  constructor(data: Partial<ActiveCitation> = {}) {
    super(data)
    Object.assign(this, data)
  }

  get renderSummary(): string {
    return [
      ...(this.citationType?.label ? [this.citationType.label] : []),
      ...(this.court ? [`${this.court?.label}`] : []),
      ...(this.decisionDate
        ? [dayjs(this.decisionDate).format("DD.MM.YYYY")]
        : []),
      ...(this.fileNumber ? [this.fileNumber] : []),
      ...(this.documentType ? [this.documentType.label] : []),
    ].join(", ")
  }

  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  get missingRequiredFields() {
    return ActiveCitation.requiredFields.filter((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }

  get isEmpty(): boolean {
    return ActiveCitation.fields.every((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }

  get isReadOnly(): boolean {
    return false
  }

  equals(entry: ActiveCitation): boolean {
    return this.localId === entry.localId
  }

  get citationTypeIsSet(): boolean {
    return !!this.citationType?.uuid
  }

  get showSummaryOnEdit(): boolean {
    return false
  }

  private fieldIsEmpty(
    value: ActiveCitation[(typeof ActiveCitation.fields)[number]],
  ): boolean {
    return !value
  }
}

export const activeCitationLabels: { [name: string]: string } = {
  citationType: "Art der Zitierung",
  fileNumber: "Aktenzeichen",
  court: "Gericht",
  decisionDate: "Entscheidungsdatum",
}
