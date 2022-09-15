import { InputType } from "./types"
import type { InputField, InputAttributes, DropdownItem } from "./types"
import documentTypes from "@/data/documentType.json"

export function defineTextField(
  name: string,
  label: string,
  iconName: string,
  ariaLabel: string,
  type: InputType,
  required?: boolean,
  placeholder?: string,
  dropdownItems?: DropdownItem[]
): InputField {
  let inputFieldType: InputType
  let inputFieldAttributes: InputAttributes
  switch (type) {
    case InputType.DROPDOWN: {
      inputFieldType = InputType.DROPDOWN
      inputFieldAttributes = { ariaLabel, placeholder, dropdownItems }
      break
    }
    default: {
      inputFieldType = InputType.TEXT
      inputFieldAttributes = { ariaLabel, placeholder }
    }
  }
  return {
    name,
    type: inputFieldType,
    label,
    iconName,
    required,
    inputAttributes: inputFieldAttributes,
  }
}

export const coreDataFields: InputField[] = [
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
    documentTypes.items
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
