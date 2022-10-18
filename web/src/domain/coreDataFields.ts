import { InputType } from "./types"
import type { InputField, DropdownItem } from "./types"
import documentTypes from "@/data/documentType.json"
import legalEffectTypes from "@/data/legalEffectTypes.json"

export function defineTextField(
  name: string,
  label: string,
  iconName: string,
  ariaLabel: string,
  required?: boolean,
  placeholder?: string
): InputField {
  return {
    name,
    type: InputType.TEXT,
    label,
    iconName,
    required,
    inputAttributes: { ariaLabel, placeholder },
  }
}

export function defineDateField(
  name: string,
  label: string,
  iconName: string,
  ariaLabel: string,
  required?: boolean,
  hasError?: boolean,
  isInPast?: boolean
): InputField {
  return {
    name,
    type: InputType.DATE,
    label,
    iconName,
    required,
    inputAttributes: { ariaLabel, hasError, isInPast },
  }
}

export function defineDropdownField(
  name: string,
  label: string,
  iconName: string,
  ariaLabel: string,
  required?: boolean,
  placeholder?: string,
  isCombobox?: boolean,
  dropdownItems?: DropdownItem[],
  preselectedValue?: string
): InputField {
  return {
    name,
    type: InputType.DROPDOWN,
    label,
    iconName,
    required,
    inputAttributes: {
      ariaLabel,
      placeholder,
      dropdownItems,
      isCombobox,
      preselectedValue,
    },
  }
}

export const coreDataFields: InputField[] = [
  defineTextField(
    "fileNumber",
    "Aktenzeichen",
    "grid_3x3",
    "Aktenzeichen",
    true
  ),
  defineDateField(
    "decisionDate",
    "Entscheidungsdatum",
    "calendar_today",
    "Entscheidungsdatum",
    true,
    undefined
  ),
  defineTextField("courtType", "Gerichtstyp", "home", "Gerichtstyp", true),
  defineDropdownField(
    "category",
    "Dokumenttyp",
    "category",
    "Dokumenttyp",
    true,
    "Bitte auswählen",
    true,
    documentTypes.items
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
  defineDropdownField(
    "legalEffect",
    "Rechtskraft",
    "gavel",
    "Rechtskraft",
    true,
    "",
    false,
    legalEffectTypes.items,
    legalEffectTypes.items[0].value
  ),
  defineTextField(
    "inputType",
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
