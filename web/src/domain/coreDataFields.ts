import { InputType } from "./types"
import type { TextInputField } from "./types"
import { documentTypes } from "@/data/documentType"

export function defineTextField(
  name: string,
  label: string,
  iconName: string,
  ariaLabel: string,
  required?: boolean,
  placeholder?: string,
  hasDropdown?: boolean,
  dropdownValues?: string[]
): TextInputField {
  return {
    name,
    type: InputType.TEXT,
    label,
    iconName,
    required,
    inputAttributes: { ariaLabel, placeholder },
    hasDropdown,
    dropdownValues,
  }
}

export const coreDataFields: TextInputField[] = [
  defineTextField(
    "docketNumber",
    "Aktenzeichen",
    "grid_3x3",
    "Aktenzeichen",
    true
  ),
  defineTextField(
    "entscheidungsdatum",
    "Entscheidungsdatum",
    "calendar_today",
    "Entscheidungsdatum",
    true
  ),
  defineTextField("gerichtstyp", "Gerichtstyp", "home", "Gerichtstyp", true),
  defineTextField(
    "dokumenttyp",
    "Dokumenttyp",
    "category",
    "Dokumenttyp",
    true,
    undefined,
    true,
    documentTypes
  ),
  defineTextField(
    "spruchkoerper",
    "Spruchkörper",
    "people_alt",
    "Spruchkörper"
  ),
  defineTextField("ecli", "ECLI", "translate", "ECLI"),
  defineTextField("vorgang", "Vorgang", "inventory_2", "Vorgang"),
  defineTextField(
    "gerichtssitz",
    "Gerichtssitz",
    "location_on",
    "Gerichtssitz"
  ),
  defineTextField("rechtskraft", "Rechtskraft", "gavel", "Rechtskraft"),
  defineTextField(
    "eingangsart",
    "Eingangsart",
    "markunread_mailbox",
    "Eingangsart"
  ),
  defineTextField(
    "dokumentationsstelle",
    "Dokumentationsstelle",
    "school",
    "Dokumentationsstelle"
  ),
  defineTextField("region", "Region", "map", "Region"),
]
