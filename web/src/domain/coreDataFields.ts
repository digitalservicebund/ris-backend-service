import { InputType } from "./types"
import type { InputField } from "./types"

export const coreDataFields: InputField[] = [
  {
    name: "aktenzeichen",
    type: InputType.TEXT,
    label: "Aktenzeichen",
    iconName: "grid_3x3",
    required: true,
    inputAttributes: {
      ariaLabel: "Aktenzeichen",
    },
  },
  {
    name: "gerichtstyp",
    type: InputType.TEXT,
    label: "Gerichtstyp",
    iconName: "home",
    required: true,
    inputAttributes: {
      ariaLabel: "Gerichtstyp",
    },
  },
  {
    name: "dokumenttyp",
    type: InputType.TEXT,
    label: "Dokumenttyp",
    iconName: "category",
    required: true,
    inputAttributes: {
      ariaLabel: "Dokumenttyp",
    },
  },
  {
    name: "vorgang",
    type: InputType.TEXT,
    label: "Vorgang",
    iconName: "inventory_2",
    inputAttributes: {
      ariaLabel: "Vorgang",
    },
  },
  {
    name: "ecli",
    type: InputType.TEXT,
    label: "ECLI",
    iconName: "translate",
    inputAttributes: {
      ariaLabel: "ECLI",
    },
  },
  {
    name: "spruchkoerper",
    type: InputType.TEXT,
    label: "Spruchkörper",
    iconName: "people_alt",
    inputAttributes: {
      ariaLabel: "Spruchkörper",
    },
  },
  {
    name: "entscheidungsdatum",
    type: InputType.TEXT,
    label: "Entscheidungsdatum",
    iconName: "calendar_today",
    required: true,
    inputAttributes: {
      ariaLabel: "Entscheidungsdatum",
    },
  },
  {
    name: "gerichtssitz",
    type: InputType.TEXT,
    label: "Gerichtssitz",
    iconName: "location_on",
    inputAttributes: {
      ariaLabel: "Gerichtssitz",
    },
  },
  {
    name: "rechtskraft",
    type: InputType.TEXT,
    label: "Rechtskraft",
    iconName: "gavel",
    inputAttributes: {
      ariaLabel: "Rechtskraft",
    },
  },
  {
    name: "eingangsart",
    type: InputType.TEXT,
    label: "Eingangsart",
    iconName: "markunread_mailbox",
    inputAttributes: {
      ariaLabel: "Eingangsart",
    },
  },
  {
    name: "dokumentationsstelle",
    type: InputType.TEXT,
    label: "Dokumentationsstelle",
    iconName: "school",
    inputAttributes: {
      ariaLabel: "Dokumentationsstelle",
    },
  },
  {
    name: "region",
    type: InputType.TEXT,
    label: "Region",
    iconName: "map",
    inputAttributes: {
      ariaLabel: "Region",
    },
  },
]
