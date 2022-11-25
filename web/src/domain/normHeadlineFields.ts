import { InputField, InputType } from "./types"

export const normHeadlineFields: InputField[] = [
  {
    name: "longTitle",
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
]
