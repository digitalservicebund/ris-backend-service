import { InputField, InputType } from "@/domain"
import { dropdown, undefinedDropDownItems } from "@/fields/norms/fieldGenerator"

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
  {
    name: "divergentEntryIntoForceDate",
    type: InputType.DATE,
    label: "Bestimmtes abweichendes Inkrafttretedatum",
    inputAttributes: {
      ariaLabel: "Bestimmtes abweichendes Inkrafttretedatum",
      isFutureDate: true,
    },
  },
  dropdown(
    "divergentEntryIntoForceDateState",
    "Unbestimmtes abweichendes Inkrafttretedatum",
    undefinedDropDownItems
  ),
  {
    name: "entryIntoForceNormCategory",
    type: InputType.TEXT,
    label: "Art der Norm",
    inputAttributes: {
      ariaLabel: "Art der Norm",
    },
  },
]
