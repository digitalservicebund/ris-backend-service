import { InputType } from "./types"
import type { TextInputField } from "./types"

export function defineTextField(
  name: string,
  label: string,
  iconName: string,
  ariaLabel: string,
  required?: boolean,
  placeholder?: string
): TextInputField {
  return {
    name,
    type: InputType.TEXT,
    label,
    iconName,
    required,
    inputAttributes: { ariaLabel, placeholder },
  }
}

export const coreDataFields: TextInputField[] = [
  defineTextField(
    "aktenzeichen",
    "Aktenzeichen",
    "grid_3x3",
    "Aktenzeichen",
    true
  ),
  defineTextField("gerichtstyp", "Gerichtstyp", "home", "Gerichtstyp", true),
  defineTextField(
    "dokumenttyp",
    "Dokumenttyp",
    "category",
    "Dokumenttyp",
    true
  ),
  defineTextField("vorgang", "Vorgang", "inventory_2", "Vorgang"),
  defineTextField("ecli", "ECLI", "translate", "ECLI"),
  defineTextField(
    "spruchkoerper",
    "Spruchkörper",
    "people_alt",
    "Spruchkörper"
  ),
  defineTextField(
    "entscheidungsdatum",
    "Entscheidungsdatum",
    "calendar_today",
    "Entscheidungsdatum",
    true
  ),
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
