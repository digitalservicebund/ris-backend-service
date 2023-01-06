import { DropdownInputField, DropdownItem, InputType } from "@/domain"

export const undefinedDropDownItems: DropdownItem[] = [
  {
    label: "unbestimmt (unbekannt)",
    value: "UNDEFINED_UNKNOWN",
  },
  {
    label: "unbestimmt (zukünftig)",
    value: "UNDEFINED_FUTURE",
  },
  {
    label: "nicht vorhanden",
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
      items: items,
    },
  }
}
