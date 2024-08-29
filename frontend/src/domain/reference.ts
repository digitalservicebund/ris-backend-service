import dayjs from "dayjs"
import EditableListItem from "./editableListItem"
import RelatedDocumentation from "./relatedDocumentation"
import LegalPeriodical from "@/domain/legalPeriodical"

export default class Reference
  extends RelatedDocumentation
  implements EditableListItem
{
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
    super()
    Object.assign(this, data)
    if (this.id == undefined) {
      this.id = crypto.randomUUID()
    }
  }

  get renderDocumentationUnit(): string {
    if (!this.documentationUnit) {
      return ""
    }
    return [
      this.documentationUnit?.court?.label ?? "",
      this.documentationUnit?.decisionDate
        ? dayjs(this.documentationUnit.decisionDate).format("DD.MM.YYYY")
        : "",
      this.documentationUnit?.fileNumber ?? "",
      this.documentationUnit?.documentType?.label ?? "",
    ]
      .filter(Boolean)
      .join(", ")
  }

  get renderReference(): string {
    return [
      this.legalPeriodical?.abbreviation ?? this.legalPeriodicalRawValue,
      this.citation
        ? `${this.citation}${this.referenceSupplement ? ` (${this.referenceSupplement})` : ""}`
        : "",
    ]
      .filter(Boolean)
      .join(" ")
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
