import { InputField, InputType } from "@/domain"

export const entryIntoForce: InputField[] = [
  {
    name: "entryIntoForceDate",
    type: InputType.DATE,
    label: "Datum des Inkrafttretens",
    inputAttributes: {
      ariaLabel: "Datum des Inkrafttretens",
    },
  },
  {
    name: "entryIntoForceDateState",
    type: InputType.DROPDOWN,
    label: "Unbestimmtes Datum des Inkrafttretens",
    inputAttributes: {
      ariaLabel: "Unbestimmtes Datum des Inkrafttretens",
    },
  },
  {
    name: "principleEntryIntoForceDate",
    type: InputType.TEXT,
    label: "Grundsätzliches Inkrafttretedatum",
    inputAttributes: {
      ariaLabel: "Grundsätzliches Inkrafttretedatum",
    },
  },
  {
    name: "principleEntryIntoForceDateState",
    type: InputType.DROPDOWN,
    label: "Unbestimmtes Grundsätzliches Inkrafttretedatum",
    inputAttributes: {
      ariaLabel: "Unbestimmtes Grundsätzliches Inkrafttretedatum",
    },
  },
  {
    name: "divergentEntryIntoForceDate",
    type: InputType.TEXT,
    label: "Abweichendes Inkrafttretedatum",
    inputAttributes: {
      ariaLabel: "Abweichendes Inkrafttretedatum",
    },
  },
  {
    name: "divergentEntryIntoForceDateState",
    type: InputType.DROPDOWN,
    label: "Unbestimmtes Abweichendes Inkrafttretedatum",
    inputAttributes: {
      ariaLabel: "Unbestimmtes Abweichendes Inkrafttretedatum",
    },
  },
]
