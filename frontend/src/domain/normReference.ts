import EditableListItem from "./editableListItem"
import { NormAbbreviation } from "./normAbbreviation"
import SingleNorm from "./singleNorm"

export default class NormReference implements EditableListItem {
  public normAbbreviation?: NormAbbreviation
  public singleNorms?: SingleNorm[]
  public normAbbreviationRawValue?: string
  public hasForeignSource: boolean = false

  static readonly requiredFields = ["normAbbreviation"] as const
  static readonly fields = [
    "normAbbreviation",
    "normAbbreviationRawValue",
  ] as const

  constructor(data: Partial<NormReference> = {}) {
    Object.assign(this, data)
  }

  get isReadOnly(): boolean {
    return false
  }

  get renderDecision(): string {
    let result: string[]
    if (this.normAbbreviation?.abbreviation) {
      result = [`${this.normAbbreviation?.abbreviation}`]
    } else if (this.normAbbreviationRawValue) {
      result = [`${this.normAbbreviationRawValue}`]
    } else {
      result = []
    }
    return [...result].join(", ")
  }

  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  get missingRequiredFields() {
    return NormReference.requiredFields.filter((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }

  get isEmpty(): boolean {
    let isEmpty = true

    NormReference.fields.map((key) => {
      if (!this.fieldIsEmpty(this[key])) {
        isEmpty = false
      }
    })
    return isEmpty
  }

  get showSummaryOnEdit(): boolean {
    return false
  }

  private fieldIsEmpty(
    value: NormReference[(typeof NormReference.fields)[number]],
  ) {
    if (
      value === undefined ||
      !value ||
      value === null ||
      Object.keys(value).length === 0
    ) {
      return true
    }

    return false
  }
}

export const normFieldLabels: { [name: string]: string } = {
  normAbbreviation: "RIS-Abkürzung",
}
