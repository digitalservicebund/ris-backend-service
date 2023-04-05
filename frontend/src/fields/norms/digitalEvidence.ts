import { InputField, InputType } from "@/shared/components/input/types"

export const digitalEvidence: InputField[] = [
  {
    name: "digitalEvidenceLink",
    id: "digitalEvidenceLink",
    type: InputType.TEXT,
    label: "Verlinkung",
    inputAttributes: {
      ariaLabel: "Verlinkung",
    },
  },
  {
    name: "digitalEvidenceRelatedData",
    id: "digitalEvidenceRelatedData",
    type: InputType.TEXT,
    label: "Zugehörige Dateien",
    inputAttributes: {
      ariaLabel: "Zugehörige Dateien",
    },
  },
  {
    name: "digitalEvidenceExternalDataNote",
    id: "digitalEvidenceExternalDataNote",
    type: InputType.TEXT,
    label: "Hinweis auf fremde Verlinkung oder Daten",
    inputAttributes: {
      ariaLabel: "Hinweis auf fremde Verlinkung oder Daten",
    },
  },
  {
    name: "digitalEvidenceAppendix",
    id: "digitalEvidenceAppendix",
    type: InputType.TEXT,
    label: "Zusatz zum Nachweis",
    inputAttributes: {
      ariaLabel: "Zusatz zum Nachweis",
    },
  },
]
