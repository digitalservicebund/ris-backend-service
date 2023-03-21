import { InputType } from "./types"
import type { InputField } from "./types"
import comboboxItemService from "@/services/comboboxItemService"

export const proceedingDecisionFields: InputField[] = [
  {
    name: "court",
    type: InputType.COMBOBOX,
    label: "Gericht",
    required: true,
    inputAttributes: {
      ariaLabel: "Gericht Rechtszug",
      placeholder: "Gerichtstyp Gerichtsort",
      itemService: comboboxItemService.getCourts,
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
  {
    name: "documentType",
    type: InputType.COMBOBOX,
    label: "Dokumenttyp",
    inputAttributes: {
      ariaLabel: "Dokumenttyp",
      placeholder: "Bitte auswählen",
      itemService: comboboxItemService.getDocumentTypes,
    },
  },
]
