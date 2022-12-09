import { DropdownInputField, DropdownItem, InputType } from "@/domain"

export const undefinedDropDownItems: DropdownItem[] = [
  {
    text: "unbestimmt (unbekannt)",
    value: "UNDEFINED_UNKNOWN",
  },
  {
    text: "unbestimmt (zukünftig)",
    value: "UNDEFINED_FUTURE",
  },
  {
    text: "nicht vorhanden",
    value: "UNDEFINED_NOT_PRESENT",
  },
]

export function dropdown(
  name: string,
  label: string,
  items: DropdownItem[]
): DropdownInputField {
  return {
    name: name,
    type: InputType.DROPDOWN,
    label: label,
    inputAttributes: {
      ariaLabel: label,
      isCombobox: true,
      dropdownItems: items,
    },
  }
}
