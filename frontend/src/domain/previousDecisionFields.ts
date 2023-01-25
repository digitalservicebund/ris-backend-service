import { InputType } from "./types"
import type { InputField } from "./types"

export const previousDecisionFields: InputField[] = [
  {
    name: "courtType",
    type: InputType.TEXT,
    label: "Gerichtstyp",
    inputAttributes: {
      ariaLabel: "Gerichtstyp Rechtszug",
    },
  },
  {
    name: "courtPlace",
    type: InputType.TEXT,
    label: "Gerichtsort",
    inputAttributes: {
      ariaLabel: "Gerichtsort Rechtszug",
    },
  },
  {
    name: "date",
    type: InputType.DATE,
    label: "Datum",
    inputAttributes: {
      ariaLabel: "Datum Rechtszug",
    },
  },
  {
    name: "fileNumber",
    type: InputType.TEXT,
    label: "Aktenzeichen",
    inputAttributes: {
      ariaLabel: "Aktenzeichen Rechtszug",
    },
  },
]
