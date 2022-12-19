import { ComboboxInputField, ComboboxItem, InputType } from "@/domain"

export const undefinedDropDownItems: ComboboxItem[] = [
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
  items: ComboboxItem[]
): ComboboxInputField {
  return {
    name: name,
    type: InputType.COMBOBOX,
    label: label,
    inputAttributes: {
      ariaLabel: label,
      isCombobox: true,
      items: items,
    },
  }
}
