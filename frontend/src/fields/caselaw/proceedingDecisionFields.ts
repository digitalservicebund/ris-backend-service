import { InputType } from "../../shared/components/input/types"
import type { InputField } from "../../shared/components/input/types"
import comboboxItemService from "@/services/comboboxItemService"

export const proceedingDecisionFields: InputField[] = [
  {
    name: "court",
    id: "proceedingdecision_court",
    type: InputType.COMBOBOX,
    label: "Gericht",
    inputAttributes: {
      ariaLabel: "Gericht Rechtszug",
      placeholder: "Gerichtstyp Gerichtsort",
      itemService: comboboxItemService.getCourts,
    },
  },
  {
    name: "date",
    id: "proceedingdecision_date",
    type: InputType.DATE,
    label: "Entscheidungsdatum",
    inputAttributes: {
      ariaLabel: "Entscheidungsdatum Rechtszug",
    },
  },
  {
    name: "fileNumber",
    id: "proceedingdecision_fileNumber",
    type: InputType.TEXT,
    label: "Aktenzeichen",
    inputAttributes: {
      ariaLabel: "Aktenzeichen Rechtszug",
    },
  },
  {
    name: "documentType",
    id: "proceedingdecision_documentType",
    type: InputType.COMBOBOX,
    label: "Dokumenttyp",
    inputAttributes: {
      ariaLabel: "Dokumenttyp Rechtszug",
      placeholder: "Bitte auswählen",
      itemService: comboboxItemService.getDocumentTypes,
    },
  },
]
