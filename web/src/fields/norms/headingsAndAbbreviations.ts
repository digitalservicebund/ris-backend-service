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
  {
    name: "unofficialLongTitle",
    type: InputType.TEXT,
    label: "Nichtamtliche Langüberschrift",
    inputAttributes: {
      ariaLabel: "Nichtamtliche Langüberschrift",
    },
  },
  {
    name: "unofficialShortTitle",
    type: InputType.TEXT,
    label: "Nichtamtliche Kurzüberschrift",
    inputAttributes: {
      ariaLabel: "Nichtamtliche Kurzüberschrift",
    },
  },
  {
    name: "unofficialAbbreviation",
    type: InputType.TEXT,
    label: "Nichtamtliche Buchstabenabkürzung",
    inputAttributes: {
      ariaLabel: "Nichtamtliche Buchstabenabkürzung",
    },
  },
]
