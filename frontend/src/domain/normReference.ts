import dayjs from "dayjs"
import EditableListItem from "./editableListItem"
import { NormAbbreviation } from "./normAbbreviation"
import { ValidationError } from "@/shared/components/input/types"

export default class NormReference implements EditableListItem {
  public normAbbreviation?: NormAbbreviation
  public singleNorm?: string
  public dateOfVersion?: string
  public dateOfRelevance?: string
  public validationErrors?: ValidationError[]

  static requiredFields = ["normAbbreviation"] as const

  constructor(data: Partial<NormReference> = {}) {
    Object.assign(this, data)
  }

  get isReadOnly(): boolean {
    return false
  }

  get renderDecision(): string {
    return [
      ...(this.normAbbreviation?.abbreviation
        ? [`${this.normAbbreviation?.abbreviation}`]
        : []),
      ...(this.singleNorm ? [this.singleNorm] : []),
      ...(this.dateOfVersion
        ? [dayjs(this.dateOfVersion).format("DD.MM.YYYY")]
        : []),
      ...(this.dateOfRelevance ? [this.dateOfRelevance] : []),
    ].join(", ")
  }

  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  get missingRequiredFields(): string[] {
    return NormReference.requiredFields.filter((field) =>
      this.requiredFieldIsEmpty(this[field])
    )
  }

  private requiredFieldIsEmpty(
    value: NormReference[(typeof NormReference.requiredFields)[number]]
  ) {
    if (value === undefined || !value || value === null) {
      return true
    }

    return false
  }
}

export const normFieldLabels: { [name: string]: string } = {
  normAbbreviation: "RIS-Abkürzung",
  singleNorm: "Einzelnorm",
  dateOfVersion: "Fassungsdatum",
  dateOfRelevance: "Jahr",
}

export type SingleNormValidationInfo = {
  singleNorm: string
  normAbbreviation?: string
}
