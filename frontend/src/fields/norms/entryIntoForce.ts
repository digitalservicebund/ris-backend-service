import { dropdown, undefinedDropDownItems } from "@/fields/norms/fieldGenerator"
import { InputField, InputType } from "@/shared/components/input/types"

export const entryIntoForce: InputField[] = [
  {
    name: "entryIntoForceDate",
    id: "entryIntoForceDate",
    type: InputType.DATE,
    label: "Datum des Inkrafttretens",
    inputAttributes: {
      ariaLabel: "Datum des Inkrafttretens",
      isFutureDate: true,
    },
  },
  dropdown(
    "entryIntoForceDateState",
    "entryIntoForceDateState",
    "Unbestimmtes Datum des Inkrafttretens",
    undefinedDropDownItems
  ),
  {
    id: "principleEntryIntoForceDate",
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
    "principleEntryIntoForceDateState",
    "Unbestimmtes grundsätzliches Inkrafttretedatum",
    undefinedDropDownItems
  ),
  {
    name: "divergentEntryIntoForceDate",
    id: "divergentEntryIntoForceDate",
    type: InputType.DATE,
    label: "Bestimmtes abweichendes Inkrafttretedatum",
    inputAttributes: {
      ariaLabel: "Bestimmtes abweichendes Inkrafttretedatum",
      isFutureDate: true,
    },
  },
  dropdown(
    "divergentEntryIntoForceDateState",
    "divergentEntryIntoForceDateState",
    "Unbestimmtes abweichendes Inkrafttretedatum",
    undefinedDropDownItems
  ),
  {
    name: "entryIntoForceNormCategory",
    id: "entryIntoForceNormCategory",
    type: InputType.TEXT,
    label: "Art der Norm",
    inputAttributes: {
      ariaLabel: "Art der Norm",
    },
  },
]
