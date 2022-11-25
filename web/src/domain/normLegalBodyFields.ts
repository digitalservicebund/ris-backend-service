import { InputField, InputType } from "./types"

export const normLegalBodyFields: InputField[] = [
  {
    name: "authorEntity",
    type: InputType.TEXT,
    label: "Staat, Land, Stadt, Landkreis oder juristische Person",
    inputAttributes: {
      ariaLabel: "Staat, Land, Stadt, Landkreis oder juristische Person",
    },
  },
  {
    name: "authorDecidingBody",
    type: InputType.TEXT,
    label: "Beschließendes Organ",
    inputAttributes: {
      ariaLabel: "Beschließendes Organ",
    },
  },
  {
    name: "authorIsResolutionMajority",
    type: InputType.CHECKBOX,
    label: "Beschlussfassung mit qualifizierter Mehrheit",
    inputAttributes: {
      ariaLabel: "Beschlussfassung mit qualifizierter Mehrheit",
    },
  },
]
