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

  static readonly requiredFields = [
    "legalPeriodical",
    "citation",
    "documentationUnit",
  ] as const

  static readonly fields = [
    "legalPeriodical",
    "citation",
    "referenceSupplement",
    "documentationUnit",
  ] as const

  static readonly documentationUnitFields = [
    "court",
    "fileNumber",
    "decisionDate",
  ] as const

  static readonly allFields = [
    ...Reference.fields,
    ...Reference.documentationUnitFields,
  ] as const

  static readonly documentUnitRequiredFields = [
    "court",
    "fileNumber",
    "decisionDate",
  ] as const

  constructor(data: Partial<Reference> = {}) {
    Object.assign(this, data)

    if (this.documentationUnit) {
      this.documentationUnit = new RelatedDocumentation({
        ...data.documentationUnit,
      })
    }
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

  get renderDecision(): string {
    return [
      this.legalPeriodical?.abbreviation ?? this.legalPeriodicalRawValue,
      this.citation,
      this.referenceSupplement ? ` (${this.referenceSupplement})` : "",
    ]
      .filter(Boolean)
      .join(" ")
  }

  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  get missingRequiredFields() {
    const requiredFields = Reference.requiredFields.filter((field) =>
      this.fieldIsEmpty(this[field]),
    )

    const documentUnitRequiredFields = this.documentationUnit
      ? Reference.documentUnitRequiredFields.filter((field) =>
          this.documentationUnitFieldIsEmpty(this.documentationUnit![field]),
        )
      : []

    return [...requiredFields, ...documentUnitRequiredFields]
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

  private documentationUnitFieldIsEmpty(
    value: RelatedDocumentation[(typeof Reference.documentationUnitFields)[number]],
  ) {
    return value === undefined || !value || Object.keys(value).length === 0
  }

  get hasForeignSource(): boolean {
    return true
  }
}
