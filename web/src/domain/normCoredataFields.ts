import { InputField, InputType } from "./types"

export const normCoredataFields: InputField[] = [
  {
    name: "referenceNumber",
    type: InputType.TEXT,
    label: "Aktenzeichen",
    inputAttributes: {
      ariaLabel: "Aktenzeichen",
    },
  },
  {
    name: "publicationDate",
    type: InputType.DATE,
    label: "Veröffentlichungsdatum",
    inputAttributes: {
      ariaLabel: "Veröffentlichungsdatum",
    },
  },
  {
    name: "announcementDate",
    type: InputType.DATE,
    label: "Verkündungsdatum",
    inputAttributes: {
      ariaLabel: "Verkündungsdatum",
    },
  },
  {
    name: "citationDate",
    type: InputType.DATE,
    label: "Zitierdatum",
    inputAttributes: {
      ariaLabel: "Zitierdatum",
    },
  },
  {
    name: "frameKeywords",
    type: InputType.TEXT,
    label: "Schlagwörter im Rahmenelement",
    inputAttributes: {
      ariaLabel: "Schlagwörter im Rahmenelement",
    },
  },
]
