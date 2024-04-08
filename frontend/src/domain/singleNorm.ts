import dayjs from "dayjs"

export default class SingleNorm {
  public singleNorm?: string
  public dateOfVersion?: string
  public dateOfRelevance?: string

  static readonly requiredFields = ["singleNorm"] as const
  static readonly fields = [
    "singleNorm",
    "dateOfVersion",
    "dateOfRelevance",
  ] as const

  constructor(data: Partial<SingleNorm> = {}) {
    Object.assign(this, data)
  }

  get isReadOnly(): boolean {
    return false
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

  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  get missingRequiredFields() {
    return SingleNorm.requiredFields.filter((field) =>
      this.fieldIsEmpty(this[field]),
    )
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

  get showSummaryOnEdit(): boolean {
    return false
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
