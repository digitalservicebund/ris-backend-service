import { InputField, InputType } from "@/domain"

export const headingsAndAbbreviations: InputField[] = [
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
