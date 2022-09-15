import { InputType } from "./types"
import type { InputField } from "./types"

export const previousDecisionFields: InputField[] = [
  {
    name: "courtType",
    type: InputType.TEXT,
    label: "Gerichtstyp",
    iconName: "home",
    inputAttributes: {
      ariaLabel: "Gerichtstyp Rechtszug",
    },
  },
  {
    name: "courtPlace",
    type: InputType.TEXT,
    label: "Gerichtsort",
    iconName: "location_on",
    inputAttributes: {
      ariaLabel: "Gerichtsort Rechtszug",
    },
  },
  {
    name: "date",
    type: InputType.TEXT,
    label: "Datum",
    iconName: "calendar_today",
    inputAttributes: {
      ariaLabel: "Datum Rechtszug",
    },
  },
  {
    name: "fileNumber",
    type: InputType.TEXT,
    label: "Aktenzeichen",
    iconName: "grid_3x3",
    inputAttributes: {
      ariaLabel: "Aktenzeichen Rechtszug",
    },
  },
]
