import { InputType } from "../../shared/components/input/types"
import type { InputField } from "../../shared/components/input/types"
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
    label: "Entscheidungsdatum",
    required: true,
    inputAttributes: {
      ariaLabel: "Entscheidungsdatum Rechtszug",
    },
  },
  {
    name: "fileNumber",
    type: InputType.TEXT,
    label: "Aktenzeichen",
    required: true,
    inputAttributes: {
      ariaLabel: "Aktenzeichen Rechtszug",
    },
  },
  {
    name: "documentType",
    type: InputType.COMBOBOX,
    label: "Dokumenttyp",
    inputAttributes: {
      ariaLabel: "Dokumenttyp Rechtszug",
      placeholder: "Bitte auswählen",
      itemService: comboboxItemService.getDocumentTypes,
    },
  },
]

export const proceedingDecisionFieldLabels: { [name: string]: string } =
  Object.assign(
    {},
    ...proceedingDecisionFields.map((field) => ({
      [field.name]: field.label as string,
    }))
  )
