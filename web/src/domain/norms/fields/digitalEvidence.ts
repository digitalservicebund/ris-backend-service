import { InputField, InputType } from "@/domain"

export const digitalEvidence: InputField[] = [
  {
    name: "digitalEvidenceLink",
    type: InputType.TEXT,
    label: "Verlinkung",
    inputAttributes: {
      ariaLabel: "Verlinkung",
    },
  },
  {
    name: "digitalEvidenceRelatedData",
    type: InputType.TEXT,
    label: "Zugehörige Dateien",
    inputAttributes: {
      ariaLabel: "Zugehörige Dateien",
    },
  },
  {
    name: "digitalEvidenceExternalDataNote",
    type: InputType.TEXT,
    label: "Hinweis auf fremde Verlinkung oder Daten",
    inputAttributes: {
      ariaLabel: "Hinweis auf fremde Verlinkung oder Daten",
    },
  },
  {
    name: "digitalEvidenceAppendix",
    type: InputType.TEXT,
    label: "Zusatz zum Nachweis",
    inputAttributes: {
      ariaLabel: "Zusatz zum Nachweis",
    },
  },
]
