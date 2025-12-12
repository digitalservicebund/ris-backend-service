import EditableListItem from "./editableListItem"
import { CurrencyCode } from "@/domain/objectValue"

export enum Addressee {
  BEVOLLMAECHTIGTER = "BEVOLLMAECHTIGTER",
  BESCHWERDEFUEHRER_ANTRAGSTELLER = "BESCHWERDEFUEHRER_ANTRAGSTELLER",
}

export const AddresseeLabels: Record<Addressee, string> = {
  [Addressee.BEVOLLMAECHTIGTER]: "Bevollmächtigter",
  [Addressee.BESCHWERDEFUEHRER_ANTRAGSTELLER]:
    "Beschwerdeführer / Antragsteller",
}

export default class AbuseFee implements EditableListItem {
  public id?: string // BE only
  public localId: string // FE only
  public amount?: number
  public currencyCode?: CurrencyCode
  public addressee?: Addressee

  static readonly requiredFields = ["amount", "addressee"] as const

  static readonly fields = ["amount", "currencyCode", "addressee"] as const

  constructor(data: Partial<AbuseFee> = {}) {
    Object.assign(this, data)
    this.localId = data.localId ?? crypto.randomUUID()
  }

  get renderSummary(): string {
    return (
      [this.amount?.toLocaleString("de-DE"), this.currencyCode?.label]
        .filter(Boolean)
        .join(` `) +
      (this.addressee ? `, ${AddresseeLabels[this.addressee]}` : "")
    )
  }

  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  get missingRequiredFields() {
    return AbuseFee.requiredFields.filter((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }

  get isEmpty(): boolean {
    return AbuseFee.fields.every((field) => this.fieldIsEmpty(this[field]))
  }

  equals(entry: AbuseFee): boolean {
    return this.localId === entry.localId
  }

  private fieldIsEmpty(
    value: AbuseFee[(typeof AbuseFee.fields)[number]],
  ): boolean {
    return !value
  }
}
