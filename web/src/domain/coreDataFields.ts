import { InputType } from "./types"
import type { TextInputField, BaseInputField, DropDownField } from "./types"
import { documentTypes } from "@/data/documentType"

export function defineTextField(
  name: string,
  label: string,
  iconName: string,
  ariaLabel: string,
  type: InputType,
  required?: boolean,
  placeholder?: string,
  dropdownValue?: string[]
): BaseInputField {
  switch (type) {
    case InputType.DROPDOWN:
      return {
        name,
        type: InputType.DROPDOWN,
        label,
        iconName,
        required,
        inputAttributes: { ariaLabel, placeholder, dropdownValue },
      } as DropDownField
    default:
      return {
        name,
        type: InputType.TEXT,
        label,
        iconName,
        required,
        inputAttributes: { ariaLabel, placeholder },
      } as TextInputField
  }
}

export const coreDataFields: BaseInputField[] = [
  defineTextField(
    "docketNumber",
    "Aktenzeichen",
    "grid_3x3",
    "Aktenzeichen",
    InputType.TEXT,
    true
  ),
  defineTextField(
    "decisionDate",
    "Entscheidungsdatum",
    "calendar_today",
    "Entscheidungsdatum",
    InputType.TEXT,
    true
  ),
  defineTextField(
    "courtType",
    "Gerichtstyp",
    "home",
    "Gerichtstyp",
    InputType.TEXT,
    true
  ),
  defineTextField(
    "category",
    "Dokumenttyp",
    "category",
    "Dokumenttyp",
    InputType.DROPDOWN,
    true,
    "Bitte auswählen",
    documentTypes
  ),
  defineTextField(
    "appraisalBody",
    "Spruchkörper",
    "people_alt",
    "Spruchkörper",
    InputType.TEXT
  ),
  defineTextField("ecli", "ECLI", "translate", "ECLI", InputType.TEXT),
  defineTextField(
    "procedure",
    "Vorgang",
    "inventory_2",
    "Vorgang",
    InputType.TEXT
  ),
  defineTextField(
    "courtLocation",
    "Gerichtssitz",
    "location_on",
    "Gerichtssitz",
    InputType.TEXT
  ),
  defineTextField(
    "legalEffect",
    "Rechtskraft",
    "gavel",
    "Rechtskraft",
    InputType.TEXT
  ),
  defineTextField(
    "receiptType",
    "Eingangsart",
    "markunread_mailbox",
    "Eingangsart",
    InputType.TEXT
  ),
  defineTextField(
    "center",
    "Dokumentationsstelle",
    "school",
    "Dokumentationsstelle",
    InputType.TEXT
  ),
  defineTextField("region", "Region", "map", "Region", InputType.TEXT),
]
