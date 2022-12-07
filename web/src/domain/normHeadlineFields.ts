import { InputField, InputType } from "./types"

export const normHeadlineFields: InputField[] = [
  {
    name: "officialLongTitle",
    type: InputType.TEXT,
    label: "Amtliche Langüberschrift",
    inputAttributes: {
      ariaLabel: "Amtliche Langüberschrift",
    },
  },
  {
    name: "officialShortTitle",
    type: InputType.TEXT,
    label: "Amtliche Kurzüberschrift",
    inputAttributes: {
      ariaLabel: "Amtliche Kurzüberschrift",
    },
  },
  {
    name: "officialAbbreviation",
    type: InputType.TEXT,
    label: "Amtliche Buchstabenabkürzung",
    inputAttributes: {
      ariaLabel: "Amtliche Buchstabenabkürzung",
    },
  },
  {
    name: "risAbbreviation",
    type: InputType.TEXT,
    label: "RIS-Abkürzung",
    inputAttributes: {
      ariaLabel: "RIS-Abkürzung",
    },
  },
]
