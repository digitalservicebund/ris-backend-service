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
  public id?: string // BE only
  public localId: string // FE only
  public newEntry?: boolean
  public languageCode?: LanguageCode
  public translationType?: TranslationType
  public translators?: string[]
  public borderNumbers?: number[]
  public urls?: string[]

  constructor(data: Partial<OriginOfTranslation> = {}) {
    Object.assign(this, data)
    this.localId = data.localId ?? crypto.randomUUID()
  }

  get isEmpty(): boolean {
    return !this["languageCode"]
  }

  equals(entry: OriginOfTranslation): boolean {
    return this.localId === entry.localId
  }
}
