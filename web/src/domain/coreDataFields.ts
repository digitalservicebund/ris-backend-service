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
    "decisionDate",
    "Entscheidungsdatum",
    "calendar_today",
    "Entscheidungsdatum",
    true
  ),
  defineTextField("courtType", "Gerichtstyp", "home", "Gerichtstyp", true),
  defineTextField(
    "category",
    "Dokumenttyp",
    "category",
    "Dokumenttyp",
    true,
    undefined,
    true,
    documentTypes
  ),
  defineTextField(
    "appraisalBody",
    "Spruchkörper",
    "people_alt",
    "Spruchkörper"
  ),
  defineTextField("ecli", "ECLI", "translate", "ECLI"),
  defineTextField("procedure", "Vorgang", "inventory_2", "Vorgang"),
  defineTextField(
    "courtLocation",
    "Gerichtssitz",
    "location_on",
    "Gerichtssitz"
  ),
  defineTextField("legalEffect", "Rechtskraft", "gavel", "Rechtskraft"),
  defineTextField(
    "receiptType",
    "Eingangsart",
    "markunread_mailbox",
    "Eingangsart"
  ),
  defineTextField(
    "center",
    "Dokumentationsstelle",
    "school",
    "Dokumentationsstelle"
  ),
  defineTextField("region", "Region", "map", "Region"),
]
