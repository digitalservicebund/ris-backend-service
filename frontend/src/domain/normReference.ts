import dayjs from "dayjs"
import { NormAbbreviation } from "./normAbbreviation"

export default class NormReference {
  public normAbbreviation?: NormAbbreviation
  public singleNorm?: string
  public dateOfVersion?: string
  public dateOfRelevance?: string

  static requiredFields = ["normAbbreviation"] as const

  constructor(data: Partial<NormReference> = {}) {
    Object.assign(this, data)
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

  get missingRequiredFields() {
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
