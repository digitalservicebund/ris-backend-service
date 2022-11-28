import { InputField, InputType } from "./types"

export const unofficialNormHeadlineFields: InputField[] = [
  {
    name: "unofficialTitle",
    type: InputType.TEXT,
    label: "Nichtamtliche Langüberschrift",
    inputAttributes: {
      ariaLabel: "Amtliche Langüberschrift",
    },
  },
  {
    name: "unofficialShortTitle",
    type: InputType.TEXT,
    label: "Nichtamtliche Kurzüberschrift",
    inputAttributes: {
      ariaLabel: "Amtliche Kurzüberschrift",
    },
  },
  {
    name: "unofficialAbbreviation",
    type: InputType.TEXT,
    label: "Nichtamtliche Buchstabenabkürzung",
    inputAttributes: {
      ariaLabel: "Amtliche Buchstabenabkürzung",
    },
  },
]
