import dayjs from "dayjs"
import { CitationStyle } from "./citationStyle"
import EditableListItem from "./editableListItem"
import RelatedDocumentation from "./relatedDocumentation"

export default class ActiveCitation
  extends RelatedDocumentation
  implements EditableListItem
{
  public citationStyle?: CitationStyle

  static requiredFields = [
    "citationStyle",
    "fileNumber",
    "court",
    "decisionDate",
  ] as const

  static fields = [
    "citationStyle",
    "fileNumber",
    "court",
    "decisionDate",
    "documentType",
  ] as const

  constructor(data: Partial<ActiveCitation> = {}) {
    super()
    Object.assign(this, data)
  }

  get renderDecision(): string {
    return [
      ...(this.citationStyle?.label ? [this.citationStyle.label] : []),
      ...(this.court?.label ? [`${this.court?.label}`] : []),
      ...(this.decisionDate
        ? [dayjs(this.decisionDate).format("DD.MM.YYYY")]
        : []),
      ...(this.fileNumber ? [this.fileNumber] : []),
      ...(this.documentType ? [this.documentType.label] : []),
      // ...(this.documentNumber && this.hasForeignSource
      //   ? [this.documentNumber]
      //   : []),
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

  get citationStyleIsSet(): boolean {
    return !!this.citationStyle?.uuid
  }

  private fieldIsEmpty(
    value: ActiveCitation[(typeof ActiveCitation.fields)[number]],
  ): boolean {
    return !value
  }
}

export const activeCitationLabels: { [name: string]: string } = {
  citationStyle: "Art der Zitierung",
  fileNumber: "Aktenzeichen",
  court: "Gericht",
  decisionDate: "Entscheidungsdatum",
}
