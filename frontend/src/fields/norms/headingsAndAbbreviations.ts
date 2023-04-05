import { InputField, InputType } from "@/shared/components/input/types"

export const headingsAndAbbreviations: InputField[] = [
  {
    name: "officialShortTitle",
    id: "officialShortTitle",
    type: InputType.TEXT,
    label: "Amtliche Kurzüberschrift",
    inputAttributes: {
      ariaLabel: "Amtliche Kurzüberschrift",
    },
  },
  {
    name: "officialAbbreviation",
    id: "officialAbbreviation",
    type: InputType.TEXT,
    label: "Amtliche Buchstabenabkürzung",
    inputAttributes: {
      ariaLabel: "Amtliche Buchstabenabkürzung",
    },
  },
]
