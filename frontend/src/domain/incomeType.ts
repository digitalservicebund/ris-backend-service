import EditableListItem from "./editableListItem"
import { DropdownItem } from "@/components/input/types"

export default class IncomeType implements EditableListItem {
  public id?: string // BE only
  public localId: string // FE only
  public typeOfIncome?: TypeOfIncome
  public terminology?: string

  static readonly fields = ["typeOfIncome", "terminology"] as const

  constructor(data: Partial<IncomeType> = {}) {
    Object.assign(this, data)
    this.localId = data.localId ?? crypto.randomUUID()
  }

  get isEmpty(): boolean {
    return IncomeType.fields.every((field) => this.fieldIsEmpty(this[field]))
  }

  private fieldIsEmpty(
    value: IncomeType[(typeof IncomeType.fields)[number]],
  ): boolean {
    return !value
  }
}

export enum TypeOfIncome {
  LAND_UND_FORTWIRTSCHAFT = "LAND_UND_FORTWIRTSCHAFT",
  GEWERBEBETRIEB = "GEWERBEBETRIEB",
  SELBSTSTAENDIGE_ARBEIT = "SELBSTSTAENDIGE_ARBEIT",
  NICHTSELBSTSTAENDIGE_ARBEIT = "NICHTSELBSTSTAENDIGE_ARBEIT",
  KAPITALVERMOEGEN = "KAPITALVERMOEGEN",
  VERMIETUNG_UND_VERPACHTUNG = "VERMIETUNG_UND_VERPACHTUNG",
  SONSTIGE_EINKUENFTE = "SONSTIGE_EINKUENFTE",
  ESTG = "ESTG",
  GEWSTG = "GEWSTG",
  USTG = "USTG",
}

export const TypeOfIncomeLabels: Record<TypeOfIncome, string> = {
  [TypeOfIncome.LAND_UND_FORTWIRTSCHAFT]: "Land- und Forstwirtschaft",
  [TypeOfIncome.GEWERBEBETRIEB]: "Gewerbebetrieb",
  [TypeOfIncome.SELBSTSTAENDIGE_ARBEIT]: "Selbständige Arbeit",
  [TypeOfIncome.NICHTSELBSTSTAENDIGE_ARBEIT]: "Nichtselbständige Arbeit",
  [TypeOfIncome.KAPITALVERMOEGEN]: "Kapitalvermögen",
  [TypeOfIncome.VERMIETUNG_UND_VERPACHTUNG]: "Vermietung und Verpachtung",
  [TypeOfIncome.SONSTIGE_EINKUENFTE]: "Sonstige Einkünfte",
  [TypeOfIncome.ESTG]: "EStG",
  [TypeOfIncome.GEWSTG]: "GewStG",
  [TypeOfIncome.USTG]: "UStG",
}

export const typeOfIncomeItems: DropdownItem[] = [
  {
    label: TypeOfIncomeLabels[TypeOfIncome.LAND_UND_FORTWIRTSCHAFT],
    value: TypeOfIncome.LAND_UND_FORTWIRTSCHAFT,
  },
  {
    label: TypeOfIncomeLabels[TypeOfIncome.GEWERBEBETRIEB],
    value: TypeOfIncome.GEWERBEBETRIEB,
  },
  {
    label: TypeOfIncomeLabels[TypeOfIncome.SELBSTSTAENDIGE_ARBEIT],
    value: TypeOfIncome.SELBSTSTAENDIGE_ARBEIT,
  },
  {
    label: TypeOfIncomeLabels[TypeOfIncome.NICHTSELBSTSTAENDIGE_ARBEIT],
    value: TypeOfIncome.NICHTSELBSTSTAENDIGE_ARBEIT,
  },
  {
    label: TypeOfIncomeLabels[TypeOfIncome.KAPITALVERMOEGEN],
    value: TypeOfIncome.KAPITALVERMOEGEN,
  },
  {
    label: TypeOfIncomeLabels[TypeOfIncome.VERMIETUNG_UND_VERPACHTUNG],
    value: TypeOfIncome.VERMIETUNG_UND_VERPACHTUNG,
  },
  {
    label: TypeOfIncomeLabels[TypeOfIncome.SONSTIGE_EINKUENFTE],
    value: TypeOfIncome.SONSTIGE_EINKUENFTE,
  },
  {
    label: TypeOfIncomeLabels[TypeOfIncome.ESTG],
    value: TypeOfIncome.ESTG,
  },
  {
    label: TypeOfIncomeLabels[TypeOfIncome.GEWSTG],
    value: TypeOfIncome.GEWSTG,
  },
  {
    label: TypeOfIncomeLabels[TypeOfIncome.USTG],
    value: TypeOfIncome.USTG,
  },
]
