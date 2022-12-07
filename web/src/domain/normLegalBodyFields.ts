import { InputField, InputType } from "./types"

export const normLegalBodyFields: InputField[] = [
  {
    name: "providerEntity",
    type: InputType.TEXT,
    label: "Staat, Land, Stadt, Landkreis oder juristische Person",
    inputAttributes: {
      ariaLabel: "Staat, Land, Stadt, Landkreis oder juristische Person",
    },
  },
  {
    name: "providerDecidingBody",
    type: InputType.TEXT,
    label: "Beschließendes Organ",
    inputAttributes: {
      ariaLabel: "Beschließendes Organ",
    },
  },
  {
    name: "providerIsResolutionMajority",
    type: InputType.CHECKBOX,
    label: "Beschlussfassung mit qualifizierter Mehrheit",
    inputAttributes: {
      ariaLabel: "Beschlussfassung mit qualifizierter Mehrheit",
    },
  },
]
