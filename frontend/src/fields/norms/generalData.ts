import { defineChipsField } from "@/fields/caselaw"
import { InputField, InputType } from "@/shared/components/input/types"

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
  defineChipsField(
    "risAbbreviationInternationalLaw",
    "Juris-Abkürzung für völkerrechtliche Vereinbarungen",
    "Juris-Abkürzung für völkerrechtliche Vereinbarungen"
  ),
  {
    name: "documentNumber",
    type: InputType.TEXT,
    label: "Dokumentnummer",
    inputAttributes: {
      ariaLabel: "Dokumentnummer",
    },
  },
  defineChipsField(
    "divergentDocumentNumber",
    "Abweichende Dokumentnummer",
    "Abweichende Dokumentnummer"
  ),
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
