import EditableListItem from "./editableListItem"

export type LanguageCode = {
  id: string
  label: string
}

export enum TranslationType {
  AMTLICH = "AMTLICH",
  NICHT_AMTLICH = "NICHT_AMTLICH",
  KEINE_ANGABE = "KEINE_ANGABE",
}

export const TranslationTypeLabels: Record<TranslationType, string> = {
  [TranslationType.AMTLICH]: "amtlich",
  [TranslationType.NICHT_AMTLICH]: "nicht-amtlich",
  [TranslationType.KEINE_ANGABE]: "keine Angabe",
}

export default class OriginOfTranslation implements EditableListItem {
  public id?: string
  public newEntry?: boolean
  public languageCode?: LanguageCode
  public translationType?: TranslationType
  public translators?: string[]
  public borderNumbers?: number[]
  public urls?: string[]

  static readonly fields = ["languageCode"] as const

  constructor(data: Partial<OriginOfTranslation> = {}) {
    Object.assign(this, data)

    if (this.id == undefined) {
      this.id = crypto.randomUUID()
      this.newEntry = true
    } else if (data.newEntry == undefined) {
      this.newEntry = false
    }
  }

  get isEmpty(): boolean {
    return OriginOfTranslation.fields.some((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }

  equals(entry: OriginOfTranslation): boolean {
    return this.id === entry.id
  }

  private fieldIsEmpty(
    value: OriginOfTranslation[(typeof OriginOfTranslation.fields)[number]],
  ): boolean {
    return !value
  }
}
