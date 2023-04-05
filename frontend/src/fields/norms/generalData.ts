import { defineChipsField } from "@/fields/caselaw"
import { InputField, InputType } from "@/shared/components/input/types"

export const generalData: InputField[] = [
  {
    name: "officialLongTitle",
    id: "officialLongTitle",
    type: InputType.TEXT,
    label: "Amtliche Langüberschrift",
    inputAttributes: {
      ariaLabel: "Amtliche Langüberschrift",
    },
  },
  {
    name: "risAbbreviation",
    id: "risAbbreviation",
    type: InputType.TEXT,
    label: "Juris-Abkürzung",
    inputAttributes: {
      ariaLabel: "Juris-Abkürzung",
    },
  },
  defineChipsField(
    "risAbbreviationInternationalLaw",
    "risAbbreviationInternationalLaw",
    "Juris-Abkürzung für völkerrechtliche Vereinbarungen",
    "Juris-Abkürzung für völkerrechtliche Vereinbarungen"
  ),
  {
    name: "documentNumber",
    id: "documentNumber",
    type: InputType.TEXT,
    label: "Dokumentnummer",
    inputAttributes: {
      ariaLabel: "Dokumentnummer",
    },
  },
  defineChipsField(
    "divergentDocumentNumber",
    "divergentDocumentNumber",
    "Abweichende Dokumentnummer",
    "Abweichende Dokumentnummer"
  ),
  {
    name: "documentCategory",
    id: "documentCategory",
    type: InputType.TEXT,
    label: "Dokumentart",
    inputAttributes: {
      ariaLabel: "Dokumentart",
    },
  },
  defineChipsField(
    "frameKeywords",
    "frameKeywords",
    "Schlagwörter im Rahmenelement",
    "Schlagwörter im Rahmenelement"
  ),
]
