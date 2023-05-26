import { dropdown, undefinedDropDownItems } from "@/fields/norms/fieldGenerator"
import { InputField, InputType } from "@/shared/components/input/types"

export const entryIntoForce: InputField[] = [
  {
    name: "entryIntoForceDate",
    type: InputType.DATE,
    label: "Datum des Inkrafttretens",
    inputAttributes: {
      ariaLabel: "Datum des Inkrafttretens",
      isFutureDate: true,
    },
  },
  dropdown(
    "entryIntoForceDateState",
    "Unbestimmtes Datum des Inkrafttretens",
    undefinedDropDownItems
  ),
  {
    name: "principleEntryIntoForceDate",
    type: InputType.DATE,
    label: "Grundsätzliches Inkrafttretedatum",
    inputAttributes: {
      ariaLabel: "Grundsätzliches Inkrafttretedatum",
      isFutureDate: true,
    },
  },
  dropdown(
    "principleEntryIntoForceDateState",
    "Unbestimmtes grundsätzliches Inkrafttretedatum",
    undefinedDropDownItems
  ),
]
