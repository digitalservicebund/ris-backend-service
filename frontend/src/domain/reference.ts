import dayjs from "dayjs"
import EditableListItem from "./editableListItem"
import RelatedDocumentation from "./relatedDocumentation"
import LegalPeriodical from "@/domain/legalPeriodical"

export default class Reference implements EditableListItem {
  id?: string
  citation?: string
  referenceSupplement?: string
  footnote?: string
  legalPeriodical?: LegalPeriodical
  legalPeriodicalRawValue?: string
  documentationUnit?: RelatedDocumentation

  static readonly requiredFields = ["legalPeriodical", "citation"] as const
  static readonly fields = [
    "legalPeriodical",
    "citation",
    "referenceSupplement",
    "court",
    "fileNumber",
    "decisionDate",
    "documentType",
  ] as const

  constructor(data: Partial<Reference> = {}) {
    Object.assign(this, data)
    if (this.id == undefined) {
      this.id = crypto.randomUUID()
    }
  }

  get renderDecision(): string {
    const firstPart = [
      ...(this.legalPeriodical
        ? [this.legalPeriodical.abbreviation]
        : [this.legalPeriodicalRawValue]),
      ...(this.citation && this.referenceSupplement
        ? [`${this.citation} (${this.referenceSupplement})`]
        : [this.citation]),
    ].join(" ")

    const secondPart = [
      ...(this.documentationUnit && this.documentationUnit.court
        ? [this.documentationUnit.court.label]
        : []),
      ...(this.documentationUnit && this.documentationUnit.decisionDate
        ? [dayjs(this.documentationUnit.decisionDate).format("DD.MM.YYYY")]
        : []),
      ...(this.documentationUnit && this.documentationUnit.fileNumber
        ? [this.documentationUnit.fileNumber]
        : []),
      ...(this.documentationUnit && this.documentationUnit.documentType
        ? [this.documentationUnit.documentType.label]
        : []),
    ].join(", ")

    return [firstPart, secondPart].filter(Boolean).join(" | ")
  }

  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  get missingRequiredFields() {
    return Reference.requiredFields.filter((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }

  equals(entry: Reference): boolean {
    return this.id === entry.id
  }

  get isEmpty(): boolean {
    let isEmpty = true

    Reference.fields.map((key) => {
      if (!this.fieldIsEmpty(this[key])) {
        isEmpty = false
      }
    })
    return isEmpty
  }

  private fieldIsEmpty(value: Reference[(typeof Reference.fields)[number]]) {
    return value === undefined || !value || Object.keys(value).length === 0
  }
}
