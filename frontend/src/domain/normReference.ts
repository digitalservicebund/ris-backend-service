import dayjs from "dayjs"
import EditableListItem from "./editableListItem"
import { NormAbbreviation } from "./normAbbreviation"
import documentUnitService from "@/services/documentUnitService"

export default class NormReference implements EditableListItem {
  public normAbbreviation?: NormAbbreviation
  public singleNorm?: string
  public dateOfVersion?: string
  public dateOfRelevance?: string
  private validationError = false

  static requiredFields = ["normAbbreviation"] as const
  static fields = [
    "normAbbreviation",
    "singleNorm",
    "dateOfVersion",
    "dateOfRelevance",
  ] as const

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

  public async updateValidationErrors(): Promise<boolean> {
    //validate singleNorm
    if (this.singleNorm) {
      const singleNormValidationInfo: SingleNormValidationInfo = {
        singleNorm: this.singleNorm,
        normAbbreviation: this.normAbbreviation?.abbreviation,
      }
      const response = await documentUnitService.validateSingleNorm(
        singleNormValidationInfo,
      )

      if (response.data !== "Ok") {
        this.validationError = true
        return true
      } else {
        this.validationError = false
        return false
      }
    } else {
      this.validationError = false
      return false
    }
  }

  get hasValidationErrors(): boolean {
    return this.validationError
  }

  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  get missingRequiredFields(): string[] {
    return NormReference.requiredFields.filter((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }

  get isEmpty(): boolean {
    let isEmpty = true

    NormReference.fields.map((key) => {
      this.fieldIsEmpty(this[key])
      if (!this.fieldIsEmpty(this[key])) {
        isEmpty = false
      }
    })
    return isEmpty
  }

  private fieldIsEmpty(
    value: NormReference[(typeof NormReference.fields)[number]],
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
