import dayjs from "dayjs"
import { capitalize } from "vue"
import LegalForce from "./legalForce"

export default class SingleNorm {
  public singleNorm?: string
  public dateOfVersion?: string
  public dateOfRelevance?: string
  public legalForce?: LegalForce

  static readonly fields = [
    "singleNorm",
    "dateOfVersion",
    "dateOfRelevance",
    "legalForce",
  ] as const

  constructor(data: Partial<SingleNorm> = {}) {
    Object.assign(this, data)
  }

  get renderDecision(): string {
    return [
      ...(this.singleNorm ? [this.singleNorm] : []),
      ...(this.dateOfVersion
        ? [dayjs(this.dateOfVersion).format("DD.MM.YYYY")]
        : []),
      ...(this.dateOfRelevance ? [this.dateOfRelevance] : []),
    ].join(", ")
  }

  get renderLegalForce(): string {
    return [
      ...(this.legalForce?.type
        ? [capitalize(this.legalForce.type.abbreviation)]
        : []),
      ...(this.legalForce?.region
        ? [`(${this.legalForce.region.longText})`]
        : []),
    ].join(" ")
  }

  get isEmpty(): boolean {
    let isEmpty = true

    SingleNorm.fields.map((key) => {
      if (!this.fieldIsEmpty(this[key])) {
        isEmpty = false
      }
    })
    return isEmpty
  }

  private fieldIsEmpty(value: SingleNorm[(typeof SingleNorm.fields)[number]]) {
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
  singleNorm: "Einzelnorm",
  dateOfVersion: "Fassungsdatum",
  dateOfRelevance: "Jahr",
}

export type SingleNormValidationInfo = {
  singleNorm: string
  normAbbreviation?: string
}
