import { defineChipsField, InputField, InputType } from "@/domain"

export const generalData: InputField[] = [
  {
    name: "officialLongTitle",
    type: InputType.TEXT,
    label: "Amtliche Langüberschrift",
    inputAttributes: {
      ariaLabel: "Amtliche Langüberschrift",
    },
  },
  {
    name: "risAbbreviation",
    type: InputType.TEXT,
    label: "Juris-Abkürzung",
    inputAttributes: {
      ariaLabel: "Juris-Abkürzung",
    },
  },
  {
    name: "risAbbreviationInternationalLaw",
    type: InputType.TEXT,
    label: "Juris-Abkürzung für völkerrechtliche Vereinbarungen",
    inputAttributes: {
      ariaLabel: "Juris-Abkürzung für völkerrechtliche Vereinbarungen",
    },
  },
  {
    name: "documentNumber",
    type: InputType.TEXT,
    label: "Dokumentnummer",
    inputAttributes: {
      ariaLabel: "Dokumentnummer",
    },
  },
  {
    name: "divergentDocumentNumber",
    type: InputType.TEXT,
    label: "Abweichende Dokumentnummer",
    inputAttributes: {
      ariaLabel: "Abweichende Dokumentnummer",
    },
  },
  {
    name: "documentCategory",
    type: InputType.TEXT,
    label: "Dokumentart",
    inputAttributes: {
      ariaLabel: "Dokumentart",
    },
  },
  defineChipsField(
    "frameKeywords",
    "Schlagwörter im Rahmenelement",
    "Schlagwörter im Rahmenelement"
  ),
]
