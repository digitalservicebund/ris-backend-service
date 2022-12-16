import { InputField, InputType } from "@/domain"

export const headingsAndAbbreviationsUnofficial: InputField[] = [
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
